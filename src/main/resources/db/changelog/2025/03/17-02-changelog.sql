-- liquibase formatted sql

-- changeset Tim:1742233904286-3
ALTER TABLE matches
    ADD max_retries INTEGER;

-- changeset Tim:1742233904286-4
ALTER TABLE matches
    ALTER COLUMN max_retries SET NOT NULL;

-- changeset Tim:1742233904286-1
ALTER TABLE room_results
    DROP COLUMN time;

-- changeset Tim:1742233904286-2
ALTER TABLE room_results
    ADD time INTEGER NOT NULL;
