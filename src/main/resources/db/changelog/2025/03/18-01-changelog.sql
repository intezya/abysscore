-- manually written cause of broken amplicode migrations


ALTER TABLE room_results
    DROP CONSTRAINT uk_room_user_match_no_retry;

ALTER TABLE room_results
    ADD CONSTRAINT uk_room_user_match_no_retry
        UNIQUE (room_number, user_id, match_id);
