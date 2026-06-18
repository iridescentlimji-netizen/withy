CREATE TABLE families (
    id          UUID        NOT NULL,
    name        VARCHAR(50) NOT NULL,
    created_by  UUID        NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_families PRIMARY KEY (id),
    CONSTRAINT fk_families_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);

CREATE TABLE family_guardians (
    id          UUID        NOT NULL,
    family_id   UUID        NOT NULL,
    user_id     UUID        NOT NULL,
    role        VARCHAR(20) NOT NULL,
    can_edit    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_family_guardians PRIMARY KEY (id),
    CONSTRAINT fk_family_guardians_family FOREIGN KEY (family_id) REFERENCES families (id) ON DELETE CASCADE,
    CONSTRAINT fk_family_guardians_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_family_guardians_role CHECK (role IN ('MASTER', 'FAMILY', 'HELPER')),
    CONSTRAINT uk_family_guardians_family_user UNIQUE (family_id, user_id)
);

CREATE INDEX idx_family_guardians_user_id ON family_guardians (user_id);
CREATE INDEX idx_family_guardians_family_id ON family_guardians (family_id);
