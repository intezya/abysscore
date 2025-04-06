-- liquibase formatted sql

-- changeset intezya:1743920798562-3
ALTER TABLE match_drafts DROP COLUMN current_state_deadline;
ALTER TABLE match_drafts DROP COLUMN penalty_time_player1;
ALTER TABLE match_drafts DROP COLUMN penalty_time_player2;
