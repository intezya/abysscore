-- liquibase formatted sql

-- changeset Tim:1742123549951-1
ALTER TABLE users
    ADD current_badge_id BIGINT;

-- changeset Tim:1742123549951-2
ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_CURRENT_BADGE FOREIGN KEY (current_badge_id) REFERENCES user_items (id);
