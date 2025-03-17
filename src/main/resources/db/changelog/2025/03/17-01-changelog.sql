-- liquibase formatted sql

-- changeset Tim:1742222589461-1
ALTER TABLE matches
    ALTER COLUMN ended_at DROP NOT NULL;
