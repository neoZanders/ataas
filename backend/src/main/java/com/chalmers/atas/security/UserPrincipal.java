package com.chalmers.atas.security;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public record UserPrincipal(
        UUID userId,
        String email,
        Collection<? extends GrantedAuthority> authorities
) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return email;
    }
}
