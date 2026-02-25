CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL,

    CONSTRAINT chk_users_user_type
        CHECK (user_type IN ('CR', 'TA'))
);

CREATE TABLE refresh_tokens (
    token_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    last_used_at TIMESTAMPTZ NULL,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
            REFERENCES users(user_id)
                ON DELETE CASCADE
);