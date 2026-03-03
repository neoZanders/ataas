package com.chalmers.atas.domain.user;

import lombok.Value;

import java.util.UUID;

@Value
public class CurrentUser {
    UUID userId;
    User user;

    public static CurrentUser of(User user) {
        return new CurrentUser(user.getUserId(), user);
    }
}
