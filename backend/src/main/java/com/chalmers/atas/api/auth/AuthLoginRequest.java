package com.chalmers.atas.api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthLoginRequest {
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
