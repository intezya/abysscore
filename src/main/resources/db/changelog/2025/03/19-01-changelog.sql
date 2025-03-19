-- liquibase formatted sql

ALTER TABLE room_results
    DROP CONSTRAINT uk_room_user_match_no_retry;

-- changeset Tim:1742388499124-1
ALTER TABLE room_results
    ADD player_id BIGINT;

-- changeset Tim:1742388499124-2
ALTER TABLE room_results
    ALTER COLUMN player_id SET NOT NULL;

-- changeset Tim:1742388499124-3
ALTER TABLE room_retries
    ADD player_id BIGINT;

-- changeset Tim:1742388499124-4
ALTER TABLE room_retries
    ALTER COLUMN player_id SET NOT NULL;

-- changeset Tim:1742388499124-5
ALTER TABLE room_results
    ADD CONSTRAINT uk_room_player_match_no_retry UNIQUE (player_id, match_id, room_number);

-- changeset Tim:1742388499124-6
ALTER TABLE room_results
    ADD CONSTRAINT FK_ROOM_RESULTS_ON_PLAYER FOREIGN KEY (player_id) REFERENCES users (id);

-- changeset Tim:1742388499124-7
ALTER TABLE room_retries
    ADD CONSTRAINT FK_ROOM_RETRIES_ON_PLAYER FOREIGN KEY (player_id) REFERENCES users (id);

-- changeset Tim:1742388499124-8
ALTER TABLE room_results
    DROP CONSTRAINT fk_room_results_on_user;

-- changeset Tim:1742388499124-9
ALTER TABLE room_retries
    DROP CONSTRAINT fk_room_retries_on_user;

-- changeset Tim:1742388499124-11
ALTER TABLE room_results
    DROP COLUMN user_id;

-- changeset Tim:1742388499124-12
ALTER TABLE room_retries
    DROP COLUMN user_id;
