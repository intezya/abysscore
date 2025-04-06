-- liquibase formatted sql

-- changeset kurumi:1743939490542-1
CREATE TABLE player1_characters (match_draft_id BIGINT NOT NULL, character_name VARCHAR(255));

-- changeset kurumi:1743939490542-2
CREATE TABLE player2_characters (match_draft_id BIGINT NOT NULL, character_name VARCHAR(255));

-- changeset kurumi:1743939490542-4
ALTER TABLE player1_characters ADD CONSTRAINT FK_PLAYER1_CHARACTERS_ON_MATCH_DRAFT FOREIGN KEY (match_draft_id) REFERENCES match_drafts (id);

-- changeset kurumi:1743939490542-5
ALTER TABLE player2_characters ADD CONSTRAINT FK_PLAYER2_CHARACTERS_ON_MATCH_DRAFT FOREIGN KEY (match_draft_id) REFERENCES match_drafts (id);

-- changeset kurumi:1743939490542-6
ALTER TABLE match_drafts DROP COLUMN player1characters;
ALTER TABLE match_drafts DROP COLUMN player2characters;
