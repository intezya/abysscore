-- liquibase formatted sql

-- changeset intezya:1743915254429-5
ALTER TABLE ban_history ADD approved_by BIGINT;
ALTER TABLE ban_history ADD dispute_approved_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE ban_history ADD dispute_reason VARCHAR(500);

-- changeset intezya:1743915254429-6
ALTER TABLE users ADD banned_until TIMESTAMP WITHOUT TIME ZONE;

-- changeset intezya:1743915254429-9
ALTER TABLE ban_history ADD CONSTRAINT FK_BAN_HISTORY_ON_APPROVED_BY FOREIGN KEY (approved_by) REFERENCES users (id);

-- changeset intezya:1743915254429-10
ALTER TABLE users DROP COLUMN blocked_until;

-- changeset intezya:1743915254429-1
ALTER TABLE ban_history ALTER COLUMN  created_at SET NOT NULL;

-- changeset intezya:1743915254429-2
ALTER TABLE ban_history ALTER COLUMN reason TYPE VARCHAR(500) USING (reason::VARCHAR(500));
