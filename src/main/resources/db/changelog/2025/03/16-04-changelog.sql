-- liquibase formatted sql

-- changeset Tim:1742115922188-1
ALTER TABLE room_results
    ADD time BIGINT;

-- changeset Tim:1742115922188-2
ALTER TABLE room_results
    ALTER COLUMN time SET NOT NULL;

-- changeset Tim:1742115922188-3
ALTER TABLE room_results
    DROP COLUMN best_time;

-- changeset Tim:1742115922188-4
ALTER TABLE matches
    DROP COLUMN matchmaking_data;
