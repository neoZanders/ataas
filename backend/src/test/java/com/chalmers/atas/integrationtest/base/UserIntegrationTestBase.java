package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

abstract class UserIntegrationTestBase {

    @Autowired
    protected UserRepository userRepository;

    protected UserBuilder createUser() {
        return new UserBuilder();
    }

    protected class UserBuilder {
        private String name = "Test User";
        private String email = unique("test-user") + "@example.com";
        private String password = "password";
        private User.UserType userType = User.UserType.TA;

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder withUserType(User.UserType userType) {
            this.userType = userType;
            return this;
        }

        public UserBuilder asCR() {
            this.userType = User.UserType.CR;
            return this;
        }

        public UserBuilder asTA() {
            this.userType = User.UserType.TA;
            return this;
        }

        protected User build() {
            return User.of(
                    email,
                    password,
                    name,
                    userType
            );
        }

        public User persist() {
            return userRepository.save(build());
        }
    }

    protected String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
