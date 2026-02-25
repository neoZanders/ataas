package com.chalmers.atas.api.auth;

import com.chalmers.atas.common.Result;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.domain.user.UserRepository;
import com.chalmers.atas.security.JwtService;
import com.chalmers.atas.security.refreshtoken.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static com.chalmers.atas.common.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-ttl-seconds:90000}") long refreshTokenTtlSeconds;

    @Value("${app.cookie.refresh.secure:false}")
    boolean refreshCookieSecure;

    @Value("${app.cookie.refresh.same-site:Strict}")
    String refreshCookieSameSite;

    public Result<AuthResponse> register(AuthRegisterRequest request, HttpServletResponse servletResponse) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return Result.error(EMAIL_TAKEN.toError());
        }

        try {
            userRepository.save(
                    User.of(
                            request.getEmail(),
                            passwordEncoder.encode(request.getPassword()),
                            request.getName(),
                            request.getUserType()
                    )
            );
        } catch (DataIntegrityViolationException e) {
            return Result.error(EMAIL_TAKEN.toError());
        }

        return login(request.getEmail(), request.getPassword(), servletResponse)
                .map(userTokenPair ->
                        AuthResponse.of(userTokenPair.getLeft(), userTokenPair.getRight()));
    }

    public Result<AuthResponse> login(AuthLoginRequest request, HttpServletResponse servletResponse) {
        return login(request.getEmail(), request.getPassword(), servletResponse)
                .map(userTokenPair ->
                        AuthResponse.of(userTokenPair.getLeft(), userTokenPair.getRight()));
    }

    public Result<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> maybeRefreshToken = extractRefreshToken(request);
        if (maybeRefreshToken.isEmpty()) {
            return Result.error(UNAUTHORIZED.toError("Missing refresh token"));
        }

        return refreshTokenService.rotateRefreshToken(maybeRefreshToken.get())
                .map(rotated -> {
                    User user = rotated.getLeft();
                    String newRefreshToken = rotated.getRight();

                    setRefreshCookie(response, newRefreshToken);

                    String newAccessToken = jwtService.generateAccessToken(
                            user.getUsername(),
                            user.getClaims()
                    );

                    return AuthResponse.of(user, newAccessToken);
                });
    }

    private Result<Pair<User, String>> login(String email, String password, HttpServletResponse servletResponse) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (AuthenticationException e) {
            return Result.error(UNAUTHORIZED.toError());
        }

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        Optional<User> maybeUser = userRepository.findByEmail(principal.getUsername());

        if (maybeUser.isEmpty()) {
            return Result.error(NOT_FOUND.toError());
        }

        User user = maybeUser.get();

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getClaims());

        Pair<User, String> refreshIssue = refreshTokenService.issueRefreshToken(user);
        setRefreshCookie(servletResponse, refreshIssue.getRight());

        return Result.ok(Pair.of(user, accessToken));
    }

    private Optional<String> extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(refreshTokenTtlSeconds))
                .sameSite(refreshCookieSameSite)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
