-- Keep one row per (series_id, start_at): prefer active rows, then earliest created.
DELETE FROM schedules
WHERE id IN (
    SELECT id
    FROM (
        SELECT s.id,
               ROW_NUMBER() OVER (
                   PARTITION BY s.series_id, s.start_at
                   ORDER BY (CASE WHEN s.cancelled THEN 1 ELSE 0 END), s.created_at, s.id
               ) AS rn
        FROM schedules s
        WHERE s.series_id IS NOT NULL
    ) ranked
    WHERE ranked.rn > 1
);

CREATE UNIQUE INDEX idx_schedules_series_start
    ON schedules (series_id, start_at);
