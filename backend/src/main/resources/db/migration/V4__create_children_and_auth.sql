CREATE TABLE children (
    id           UUID        NOT NULL,
    family_id    UUID        NOT NULL,
    nickname     VARCHAR(50) NOT NULL,
    birth_year   SMALLINT    NOT NULL,
    user_id      UUID,
    can_edit     BOOLEAN     NOT NULL DEFAULT FALSE,
    app_enabled  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_children PRIMARY KEY (id),
    CONSTRAINT fk_children_family FOREIGN KEY (family_id) REFERENCES families (id) ON DELETE CASCADE,
    CONSTRAINT fk_children_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_children_birth_year CHECK (birth_year BETWEEN 2000 AND 2100),
    CONSTRAINT uk_children_user_id UNIQUE (user_id)
);

CREATE INDEX idx_children_family_id ON children (family_id);

CREATE TABLE child_credentials (
    child_id         UUID         NOT NULL,
    pin_hash         VARCHAR(255) NOT NULL,
    failed_attempts  SMALLINT     NOT NULL DEFAULT 0,
    locked_until     TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE  NOT NULL,
    updated_at       TIMESTAMP WITH TIME ZONE  NOT NULL,
    CONSTRAINT pk_child_credentials PRIMARY KEY (child_id),
    CONSTRAINT fk_child_credentials_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE
);

CREATE TABLE child_device_sessions (
    id                 UUID         NOT NULL,
    child_id           UUID         NOT NULL,
    device_token_hash  VARCHAR(255) NOT NULL,
    device_name        VARCHAR(100),
    last_active_at     TIMESTAMP WITH TIME ZONE  NOT NULL,
    expires_at         TIMESTAMP WITH TIME ZONE,
    created_at         TIMESTAMP WITH TIME ZONE  NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE  NOT NULL,
    CONSTRAINT pk_child_device_sessions PRIMARY KEY (id),
    CONSTRAINT fk_child_device_sessions_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE,
    CONSTRAINT uk_child_device_sessions_token UNIQUE (device_token_hash)
);

CREATE INDEX idx_child_device_sessions_child_id ON child_device_sessions (child_id);
