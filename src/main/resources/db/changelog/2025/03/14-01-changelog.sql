-- liquibase formatted sql

-- changeset Tim:1741973243452-9
CREATE TABLE match_invite
(
    id                  BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    inviter_id          BIGINT                                  NOT NULL,
    invitee_id          BIGINT                                  NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    active_diff_seconds BIGINT                                  NOT NULL,
    CONSTRAINT pk_match_invite PRIMARY KEY (id)
);

-- changeset Tim:1741973243452-11
ALTER TABLE user_global_statistics
    ADD created_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE user_global_statistics
    ADD matches_draws INTEGER;
ALTER TABLE user_global_statistics
    ADD matches_lost INTEGER;
ALTER TABLE user_global_statistics
    ADD matches_won INTEGER;
ALTER TABLE user_global_statistics
    ADD skill INTEGER;
ALTER TABLE user_global_statistics
    ADD updated_at TIMESTAMP WITHOUT TIME ZONE;

-- changeset Tim:1741973243452-12
ALTER TABLE user_global_statistics
    ALTER COLUMN created_at SET NOT NULL;

-- changeset Tim:1741973243452-14
ALTER TABLE user_global_statistics
    ALTER COLUMN matches_draws SET NOT NULL;

-- changeset Tim:1741973243452-16
ALTER TABLE user_global_statistics
    ALTER COLUMN matches_lost SET NOT NULL;

-- changeset Tim:1741973243452-18
ALTER TABLE user_global_statistics
    ALTER COLUMN matches_won SET NOT NULL;

-- changeset Tim:1741973243452-19
ALTER TABLE users
    ADD receive_match_invites BOOLEAN;

-- changeset Tim:1741973243452-20
ALTER TABLE users
    ALTER COLUMN receive_match_invites SET NOT NULL;

-- changeset Tim:1741973243452-22
ALTER TABLE user_global_statistics
    ALTER COLUMN skill SET NOT NULL;

-- changeset Tim:1741973243452-24
ALTER TABLE user_global_statistics
    ALTER COLUMN updated_at SET NOT NULL;

-- changeset Tim:1741973243452-34
ALTER TABLE match_invite
    ADD CONSTRAINT FK_MATCH_INVITE_ON_INVITEE FOREIGN KEY (invitee_id) REFERENCES users (id);

-- changeset Tim:1741973243452-35
ALTER TABLE match_invite
    ADD CONSTRAINT FK_MATCH_INVITE_ON_INVITER FOREIGN KEY (inviter_id) REFERENCES users (id);

-- changeset Tim:1741973243452-43
ALTER TABLE users
    DROP CONSTRAINT fk_users_on_globalstatistic;

-- changeset Tim:1741973243452-44
ALTER TABLE user_global_statistics
    DROP COLUMN draws;
ALTER TABLE user_global_statistics
    DROP COLUMN losses;
ALTER TABLE user_global_statistics
    DROP COLUMN wins;

-- changeset Tim:1741973243452-45
ALTER TABLE users
    DROP COLUMN global_statistic_id;
