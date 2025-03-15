-- liquibase formatted sql

-- changeset Tim:1742048034569-1
ALTER TABLE user_global_statistics
    ALTER COLUMN user_id DROP NOT NULL;
