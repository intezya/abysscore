-- liquibase formatted sql

-- changeset Tim:1742114668581-1
CREATE TABLE room_results
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_id      BIGINT                                  NOT NULL,
    match_id     BIGINT                                  NOT NULL,
    room_number  INTEGER                                 NOT NULL,
    best_time    BIGINT                                  NOT NULL,
    completed_at TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT pk_room_results PRIMARY KEY (id)
);

-- changeset Tim:1742114668581-2
ALTER TABLE users
    ADD current_match_id BIGINT;

-- changeset Tim:1742114668581-3
ALTER TABLE room_results
    ADD CONSTRAINT FK_ROOM_RESULTS_ON_MATCH FOREIGN KEY (match_id) REFERENCES matches (id);

-- changeset Tim:1742114668581-4
ALTER TABLE room_results
    ADD CONSTRAINT FK_ROOM_RESULTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

-- changeset Tim:1742114668581-5
ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_CURRENT_MATCH FOREIGN KEY (current_match_id) REFERENCES matches (id);

-- changeset Tim:1742114668581-6
ALTER TABLE matches
    DROP COLUMN player1score;
ALTER TABLE matches
    DROP COLUMN player2score;
