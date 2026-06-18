CREATE TABLE schedules (
    id             UUID         NOT NULL,
    child_id       UUID         NOT NULL,
    title          VARCHAR(100) NOT NULL,
    description    VARCHAR(500),
    schedule_type  VARCHAR(30)  NOT NULL DEFAULT 'OTHER',
    start_at       TIMESTAMP WITH TIME ZONE  NOT NULL,
    end_at         TIMESTAMP WITH TIME ZONE  NOT NULL,
    created_by     UUID         NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE  NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE  NOT NULL,
    CONSTRAINT pk_schedules PRIMARY KEY (id),
    CONSTRAINT fk_schedules_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedules_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_schedules_type CHECK (schedule_type IN ('DROPOFF', 'PICKUP', 'ACTIVITY', 'OTHER')),
    CONSTRAINT ck_schedules_time CHECK (end_at > start_at)
);

CREATE INDEX idx_schedules_child_start_at ON schedules (child_id, start_at);
