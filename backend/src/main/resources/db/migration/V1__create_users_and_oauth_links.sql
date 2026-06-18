CREATE TABLE users (
    id           UUID         NOT NULL,
    nickname     VARCHAR(50)  NOT NULL,
    account_type VARCHAR(10)  NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE  NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT ck_users_account_type CHECK (account_type IN ('ADULT', 'CHILD'))
);

CREATE TABLE user_oauth_links (
    id              UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    oauth_provider  VARCHAR(20)  NOT NULL,
    oauth_subject   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL,
    CONSTRAINT pk_user_oauth_links PRIMARY KEY (id),
    CONSTRAINT fk_user_oauth_links_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_oauth_links_provider CHECK (oauth_provider IN ('KAKAO', 'NAVER', 'GOOGLE')),
    CONSTRAINT uk_user_oauth_links_provider_subject UNIQUE (oauth_provider, oauth_subject),
    CONSTRAINT uk_user_oauth_links_user_provider UNIQUE (user_id, oauth_provider)
);

CREATE INDEX idx_user_oauth_links_user_id ON user_oauth_links (user_id);
