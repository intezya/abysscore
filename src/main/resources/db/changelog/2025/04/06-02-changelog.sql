-- liquibase formatted sql

-- changeset kurumi:1743939325966-1
ALTER TABLE match_drafts ADD player1characters TEXT[];
ALTER TABLE match_drafts ADD player2characters TEXT[];

-- changeset kurumi:1743939325966-4
ALTER TABLE draft_player1_characters DROP CONSTRAINT "FK_DRAPLACHA_ON_DRAFT_CHARACTERqnFgsB";

-- changeset kurumi:1743939325966-5
ALTER TABLE draft_player1_characters DROP CONSTRAINT "FK_DRAPLACHA_ON_MATCH_DRAFTKNYHwP";

-- changeset kurumi:1743939325966-6
ALTER TABLE draft_player2_characters DROP CONSTRAINT fk_draplacha_on_draft_character;

-- changeset kurumi:1743939325966-7
ALTER TABLE draft_player2_characters DROP CONSTRAINT fk_draplacha_on_match_draft;

-- changeset kurumi:1743939325966-8
DROP TABLE draft_characters CASCADE;

-- changeset kurumi:1743939325966-9
DROP TABLE draft_player1_characters CASCADE;

-- changeset kurumi:1743939325966-10
DROP TABLE draft_player2_characters CASCADE;
