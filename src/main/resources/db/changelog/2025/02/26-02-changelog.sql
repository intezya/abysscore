-- liquibase formatted sql

-- changeset Tim:1740583902028-1
ALTER TABLE user_items
    ALTER COLUMN trade_id DROP NOT NULL;
