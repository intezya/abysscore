-- liquibase formatted sql

-- changeset intezya:1743877206628-4
ALTER TABLE users ADD blocked_until TIMESTAMP WITHOUT TIME ZONE;
