-- liquibase formatted sql

-- changeset Tim:1742393204569-3
ALTER TABLE matches
    ADD technical_defeat_reason VARCHAR(255);
