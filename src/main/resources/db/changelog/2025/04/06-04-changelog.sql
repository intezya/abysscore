-- liquibase formatted sql

-- changeset intezya:1743942602073-3
ALTER TABLE player1_characters ADD constellations INTEGER;
ALTER TABLE player1_characters ADD element VARCHAR(255);
ALTER TABLE player1_characters ADD level INTEGER;
ALTER TABLE player1_characters ADD match_id BIGINT;
ALTER TABLE player1_characters ADD name VARCHAR(255);
ALTER TABLE player1_characters ADD rarity INTEGER;

-- changeset intezya:1743942602073-4
ALTER TABLE player1_characters ALTER COLUMN  constellations SET NOT NULL;

-- changeset intezya:1743942602073-5
ALTER TABLE player2_characters ADD constellations INTEGER;
ALTER TABLE player2_characters ADD element VARCHAR(255);
ALTER TABLE player2_characters ADD level INTEGER;
ALTER TABLE player2_characters ADD match_id BIGINT;
ALTER TABLE player2_characters ADD name VARCHAR(255);
ALTER TABLE player2_characters ADD rarity INTEGER;

-- changeset intezya:1743942602073-6
ALTER TABLE player2_characters ALTER COLUMN  constellations SET NOT NULL;

-- changeset intezya:1743942602073-8
ALTER TABLE player1_characters ALTER COLUMN  element SET NOT NULL;

-- changeset intezya:1743942602073-10
ALTER TABLE player2_characters ALTER COLUMN  element SET NOT NULL;

-- changeset intezya:1743942602073-12
ALTER TABLE player1_characters ALTER COLUMN  level SET NOT NULL;

-- changeset intezya:1743942602073-14
ALTER TABLE player2_characters ALTER COLUMN  level SET NOT NULL;

-- changeset intezya:1743942602073-16
ALTER TABLE player1_characters ALTER COLUMN  match_id SET NOT NULL;

-- changeset intezya:1743942602073-18
ALTER TABLE player2_characters ALTER COLUMN  match_id SET NOT NULL;

-- changeset intezya:1743942602073-20
ALTER TABLE player1_characters ALTER COLUMN  name SET NOT NULL;

-- changeset intezya:1743942602073-22
ALTER TABLE player2_characters ALTER COLUMN  name SET NOT NULL;

-- changeset intezya:1743942602073-24
ALTER TABLE player1_characters ALTER COLUMN  rarity SET NOT NULL;

-- changeset intezya:1743942602073-26
ALTER TABLE player2_characters ALTER COLUMN  rarity SET NOT NULL;

-- changeset intezya:1743942602073-29
ALTER TABLE player1_characters DROP CONSTRAINT fk_player1_characters_on_match_draft;

-- changeset intezya:1743942602073-30
ALTER TABLE player2_characters DROP CONSTRAINT fk_player2_characters_on_match_draft;

-- changeset intezya:1743942602073-27
ALTER TABLE player1_characters ADD CONSTRAINT FK_PLAYER1_CHARACTERS_ON_MATCH_DRAFT FOREIGN KEY (match_id) REFERENCES match_drafts (id);

-- changeset intezya:1743942602073-28
ALTER TABLE player2_characters ADD CONSTRAINT FK_PLAYER2_CHARACTERS_ON_MATCH_DRAFT FOREIGN KEY (match_id) REFERENCES match_drafts (id);

-- changeset intezya:1743942602073-31
ALTER TABLE player1_characters DROP COLUMN character_name;
ALTER TABLE player1_characters DROP COLUMN match_draft_id;

-- changeset intezya:1743942602073-32
ALTER TABLE player2_characters DROP COLUMN character_name;
ALTER TABLE player2_characters DROP COLUMN match_draft_id;
