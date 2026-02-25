package com.chalmers.atas.api.auth;

import com.chalmers.atas.api.user.UserResponse;
import com.chalmers.atas.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private final UserResponse userResponse;

    private final String accessToken;

    public static AuthResponse of(User user, String accessToken) {
        return new AuthResponse(UserResponse.of(user), accessToken);
    }
}
