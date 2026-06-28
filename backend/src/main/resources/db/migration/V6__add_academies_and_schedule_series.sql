CREATE TABLE academies (
    id                         UUID         NOT NULL,
    family_id                  UUID         NOT NULL,
    name                       VARCHAR(100) NOT NULL,
    phone                      VARCHAR(30),
    default_subject_category   VARCHAR(30),
    memo                       VARCHAR(500),
    created_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                 TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_academies PRIMARY KEY (id),
    CONSTRAINT fk_academies_family FOREIGN KEY (family_id) REFERENCES families (id) ON DELETE CASCADE,
    CONSTRAINT ck_academies_default_subject_category CHECK (
        default_subject_category IS NULL OR default_subject_category IN (
            'LANGUAGE', 'MATH', 'ENGLISH', 'SOCIAL', 'SCIENCE',
            'MUSIC', 'ART', 'PE', 'SECOND_LANGUAGE', 'OTHER'
        )
    )
);

CREATE INDEX idx_academies_family_id ON academies (family_id);

CREATE TABLE schedule_series (
    id                   UUID         NOT NULL,
    child_id             UUID         NOT NULL,
    academy_id           UUID,
    title                VARCHAR(100) NOT NULL,
    description          VARCHAR(500),
    schedule_type        VARCHAR(30)  NOT NULL,
    subject_category     VARCHAR(30),
    pickup_guardian_id   UUID,
    recurrence_type      VARCHAR(20)  NOT NULL,
    days_of_week         SMALLINT,
    day_of_month         SMALLINT,
    anchor_date          DATE,
    start_time           TIME         NOT NULL,
    end_time             TIME         NOT NULL,
    effective_from       DATE         NOT NULL,
    effective_until      DATE,
    created_by           UUID         NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_schedule_series PRIMARY KEY (id),
    CONSTRAINT fk_schedule_series_child FOREIGN KEY (child_id) REFERENCES children (id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_series_academy FOREIGN KEY (academy_id) REFERENCES academies (id) ON DELETE SET NULL,
    CONSTRAINT fk_schedule_series_pickup_guardian FOREIGN KEY (pickup_guardian_id) REFERENCES users (id),
    CONSTRAINT fk_schedule_series_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_schedule_series_type CHECK (schedule_type IN ('DROPOFF', 'PICKUP', 'ACTIVITY', 'OTHER')),
    CONSTRAINT ck_schedule_series_recurrence CHECK (recurrence_type IN ('WEEKLY', 'BIWEEKLY', 'MONTHLY')),
    CONSTRAINT ck_schedule_series_subject_category CHECK (
        subject_category IS NULL OR subject_category IN (
            'LANGUAGE', 'MATH', 'ENGLISH', 'SOCIAL', 'SCIENCE',
            'MUSIC', 'ART', 'PE', 'SECOND_LANGUAGE', 'OTHER'
        )
    ),
    CONSTRAINT ck_schedule_series_time CHECK (end_time > start_time)
);

CREATE INDEX idx_schedule_series_child_id ON schedule_series (child_id);

ALTER TABLE schedules ADD COLUMN series_id UUID;
ALTER TABLE schedules ADD COLUMN academy_id UUID;
ALTER TABLE schedules ADD COLUMN subject_category VARCHAR(30);
ALTER TABLE schedules ADD COLUMN pickup_guardian_id UUID;
ALTER TABLE schedules ADD COLUMN cancelled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE schedules
    ADD CONSTRAINT fk_schedules_series FOREIGN KEY (series_id) REFERENCES schedule_series (id) ON DELETE SET NULL;

ALTER TABLE schedules
    ADD CONSTRAINT fk_schedules_academy FOREIGN KEY (academy_id) REFERENCES academies (id) ON DELETE SET NULL;

ALTER TABLE schedules
    ADD CONSTRAINT fk_schedules_pickup_guardian FOREIGN KEY (pickup_guardian_id) REFERENCES users (id);

ALTER TABLE schedules
    ADD CONSTRAINT ck_schedules_subject_category CHECK (
        subject_category IS NULL OR subject_category IN (
            'LANGUAGE', 'MATH', 'ENGLISH', 'SOCIAL', 'SCIENCE',
            'MUSIC', 'ART', 'PE', 'SECOND_LANGUAGE', 'OTHER'
        )
    );

CREATE INDEX idx_schedules_series_id ON schedules (series_id);
CREATE INDEX idx_schedules_academy_id ON schedules (academy_id);
CREATE INDEX idx_schedules_child_start_date ON schedules (child_id, start_at);
