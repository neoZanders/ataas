package com.chalmers.atas.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public Optional<CurrentUser> getMaybeCurrentUser(UUID userId) {
        return userRepository.findById(userId).map(CurrentUser::of);
    }

}
