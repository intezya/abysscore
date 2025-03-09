-- liquibase formatted sql

-- changeset Tim:1741526413670-3
ALTER TABLE users
    ADD access_level INTEGER;

-- changeset Tim:1741526413670-4
ALTER TABLE users
    ALTER COLUMN access_level SET NOT NULL;

-- changeset Tim:1741526413670-5
ALTER TABLE admins
    DROP CONSTRAINT fk_admins_on_id;

-- changeset Tim:1741526413670-7
DROP TABLE admins CASCADE;

-- changeset Tim:1741526413670-1
ALTER TABLE game_items
    ALTER COLUMN rarity SET NOT NULL;

-- changeset Tim:1741526413670-2
ALTER TABLE game_items
    ALTER COLUMN type SET NOT NULL;
