CREATE TABLE refresh_tokens(
    id BIGSERIAL PRIMARY KEY,
    token_id VARCHAR(100) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_refresh_tokens_user
                           FOREIGN KEY (user_id)
                           REFERENCES users(id)
                           ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_id ON refresh_tokens(token_id);