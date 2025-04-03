-- changeset intezya:1743523124248-3
ALTER TABLE room_results DROP CONSTRAINT uk_room_player_match_no_retry;

ALTER TABLE room_results ADD CONSTRAINT uk_room_player_match_no_retry
UNIQUE (match_id, player_id, room_number);
