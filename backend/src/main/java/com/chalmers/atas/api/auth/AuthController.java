package com.chalmers.atas.api.auth;

import com.chalmers.atas.common.HttpResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authApplicationService;

    @PostMapping("/register")
    public HttpResponse<AuthResponse> register(
            @RequestBody AuthRegisterRequest request,
            HttpServletResponse servletResponse
    ) {
        return HttpResponse.fromResult(authApplicationService.register(request, servletResponse));
    }

    @PostMapping("/login")
    public HttpResponse<AuthResponse> login(
            @RequestBody AuthLoginRequest request,
            HttpServletResponse servletResponse
    ) {
        return HttpResponse.fromResult(authApplicationService.login(request, servletResponse));
    }

    @PostMapping("/refresh")
    public HttpResponse<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return HttpResponse.fromResult(authApplicationService.refreshToken(request, response));
    }
}
