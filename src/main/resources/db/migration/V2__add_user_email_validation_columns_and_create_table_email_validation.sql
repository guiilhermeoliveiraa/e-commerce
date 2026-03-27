ALTER TABLE users
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN last_verification_email_sent_at TIMESTAMP,
ADD COLUMN verification_email_request_count INTEGER;

CREATE TABLE email_validations(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    verification_code VARCHAR(10) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,

                CONSTRAINT fk_email_validations_user
                              FOREIGN KEY (user_id)
                              REFERENCES users(id)
                              ON DELETE CASCADE
);