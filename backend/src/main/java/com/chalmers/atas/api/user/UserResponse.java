package com.chalmers.atas.api.user;

import com.chalmers.atas.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponse {

    private UUID userId;

    private String email;

    private String name;

    private User.UserType userType;

    public static UserResponse of(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getUserType()
        );
    }
}
