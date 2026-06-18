CREATE TABLE family_join_requests (
    id                UUID        NOT NULL,
    family_id         UUID        NOT NULL,
    user_id           UUID        NOT NULL,
    role              VARCHAR(20) NOT NULL,
    can_edit          BOOLEAN     NOT NULL DEFAULT FALSE,
    invite_code_hash  VARCHAR(64) NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by       UUID,
    reviewed_at       TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_family_join_requests PRIMARY KEY (id),
    CONSTRAINT fk_family_join_requests_family FOREIGN KEY (family_id) REFERENCES families (id) ON DELETE CASCADE,
    CONSTRAINT fk_family_join_requests_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_family_join_requests_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id),
    CONSTRAINT ck_family_join_requests_role CHECK (role IN ('MASTER', 'FAMILY', 'HELPER')),
    CONSTRAINT ck_family_join_requests_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED'))
);

CREATE INDEX idx_family_join_requests_family_status ON family_join_requests (family_id, status);
CREATE INDEX idx_family_join_requests_user_id ON family_join_requests (user_id);

-- Note: one PENDING request per (family_id, user_id) is enforced at application layer.
