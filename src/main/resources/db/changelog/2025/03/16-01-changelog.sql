-- liquibase formatted sql

-- changeset Tim:1742113568364-1
CREATE TABLE matches
(
    id               BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    started_at       TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    ended_at         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    status           VARCHAR(255)                            NOT NULL,
    winner_id        BIGINT,
    player1score     INTEGER                                 NOT NULL,
    player2score     INTEGER                                 NOT NULL,
    matchmaking_data VARCHAR(255),
    player1_id       BIGINT                                  NOT NULL,
    player2_id       BIGINT                                  NOT NULL,
    CONSTRAINT pk_matches PRIMARY KEY (id)
);

-- changeset Tim:1742113568364-2
ALTER TABLE matches
    ADD CONSTRAINT FK_MATCHES_ON_PLAYER1 FOREIGN KEY (player1_id) REFERENCES users (id);

-- changeset Tim:1742113568364-3
ALTER TABLE matches
    ADD CONSTRAINT FK_MATCHES_ON_PLAYER2 FOREIGN KEY (player2_id) REFERENCES users (id);

-- changeset Tim:1742113568364-4
ALTER TABLE matches
    ADD CONSTRAINT FK_MATCHES_ON_WINNER FOREIGN KEY (winner_id) REFERENCES users (id);
