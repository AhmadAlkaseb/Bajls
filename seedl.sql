BEGIN;

TRUNCATE TABLE
    gang_affiliations,
    characters,
    profiles,
    gangs,
    houses,
    roles,
    genders,
    weights,
    heights,
    eyecolors,
    skincolors
RESTART IDENTITY CASCADE;

INSERT INTO roles (id, name) VALUES
    (1, 'USER'),
    (2, 'ADMIN');

INSERT INTO genders (id, name) VALUES
    (1, 'MALE'),
    (2, 'FEMALE'),
    (3, 'OTHER');

INSERT INTO weights (id, name) VALUES
    (1, 'LIGHT'),
    (2, 'AVERAGE'),
    (3, 'HEAVY');

INSERT INTO heights (id, name) VALUES
    (1, 'SHORT'),
    (2, 'AVERAGE'),
    (3, 'TALL');

INSERT INTO eyecolors (id, name) VALUES
    (1, 'BROWN'),
    (2, 'BLUE'),
    (3, 'GREEN'),
    (4, 'GRAY');

INSERT INTO skincolors (id, name) VALUES
    (1, 'LIGHT'),
    (2, 'MEDIUM'),
    (3, 'DARK');

INSERT INTO gangs (id, name, type) VALUES
    (1, 'Iron Wolves', 'Combat'),
    (2, 'Neon Foxes', 'Stealth'),
    (3, 'Sky Raiders', 'Exploration'),
    (4, 'Stone Guard', 'Defense'),
    (5, 'Crimson Tide', 'PvP'),
    (6, 'Frost Syndicate', 'Strategy'),
    (7, 'Solar Pact', 'Support'),
    (8, 'Night Owls', 'Recon'),
    (9, 'Sand Vipers', 'Raids'),
    (10, 'Echo Unit', 'Mixed');

INSERT INTO profiles (id, first_name, last_name, email, username, password, role_id) VALUES
    (1, 'Mia', 'Hansen', 'mia@bajls.dev', 'mihansen', 'P@ssMia1', 1),
    (2, 'Noah', 'Larsen', 'noah@bajls.dev', 'nlarsen', 'P@ssNoah2', 1),
    (3, 'Emma', 'Nielsen', 'emma@bajls.dev', 'enielsen', 'P@ssEmma3', 1),
    (4, 'Lucas', 'Pedersen', 'lucas@bajls.dev', 'lpedersen', 'P@ssLucas4', 1),
    (5, 'Ida', 'Jensen', 'ida@bajls.dev', 'ijensen', 'P@ssIda5', 1),
    (6, 'Oliver', 'Madsen', 'oliver@bajls.dev', 'omadsen', 'P@ssOliver6', 1),
    (7, 'Sofia', 'Kristensen', 'sofia@bajls.dev', 'skristensen', 'P@ssSofia7', 1),
    (8, 'William', 'Olsen', 'will@bajls.dev', 'wolsen', 'P@ssWill8', 1),
    (9, 'Asta', 'Andersen', 'asta@bajls.dev', 'aandersen', 'P@ssAsta9', 2),
    (10, 'Victor', 'Soerensen', 'vic@bajls.dev', 'vsorensen', 'P@ssVic10', 2);

DO $$
DECLARE
    characters_has_house_id BOOLEAN;
    houses_has_character_id BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'characters'
          AND column_name = 'house_id'
    ) INTO characters_has_house_id;

    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'houses'
          AND column_name = 'character_id'
    ) INTO houses_has_character_id;

    IF characters_has_house_id AND NOT houses_has_character_id THEN
        INSERT INTO houses (id, amount_rooms, amount_bathrooms) VALUES
            (1, 2, 1),
            (2, 3, 2),
            (3, 1, 1),
            (4, 4, 2),
            (5, 2, 2),
            (6, 5, 3),
            (7, 3, 1),
            (8, 2, 1),
            (9, 4, 3),
            (10, 1, 1);

        INSERT INTO characters (
            id, name, balance, profile_id, gender_id, skincolor_id, eyecolor_id, height_id, weight_id, house_id
        ) VALUES
            (1, 'ShadowMia', 2450.50, 1, 2, 1, 2, 2, 2, 1),
            (2, 'SteelNoah', 1320.00, 2, 1, 2, 1, 3, 3, 2),
            (3, 'RuneEmma', 3890.75, 3, 2, 2, 3, 2, 1, 3),
            (4, 'VoltLucas', 980.25, 4, 1, 3, 4, 1, 2, 4),
            (5, 'NovaIda', 4150.00, 5, 2, 1, 1, 3, 2, 5),
            (6, 'HexOliver', 2100.40, 6, 1, 2, 2, 2, 3, 6),
            (7, 'BlazeSofia', 1750.10, 7, 2, 3, 3, 1, 1, 7),
            (8, 'FrostWill', 2999.99, 8, 1, 1, 4, 2, 2, 8),
            (9, 'AdminAsta', 5000.00, 9, 3, 2, 1, 3, 2, 9),
            (10, 'ModVictor', 4600.60, 10, 1, 3, 2, 2, 3, 10);

    ELSIF houses_has_character_id AND NOT characters_has_house_id THEN
        INSERT INTO characters (
            id, name, balance, profile_id, gender_id, skincolor_id, eyecolor_id, height_id, weight_id
        ) VALUES
            (1, 'ShadowMia', 2450.50, 1, 2, 1, 2, 2, 2),
            (2, 'SteelNoah', 1320.00, 2, 1, 2, 1, 3, 3),
            (3, 'RuneEmma', 3890.75, 3, 2, 2, 3, 2, 1),
            (4, 'VoltLucas', 980.25, 4, 1, 3, 4, 1, 2),
            (5, 'NovaIda', 4150.00, 5, 2, 1, 1, 3, 2),
            (6, 'HexOliver', 2100.40, 6, 1, 2, 2, 2, 3),
            (7, 'BlazeSofia', 1750.10, 7, 2, 3, 3, 1, 1),
            (8, 'FrostWill', 2999.99, 8, 1, 1, 4, 2, 2),
            (9, 'AdminAsta', 5000.00, 9, 3, 2, 1, 3, 2),
            (10, 'ModVictor', 4600.60, 10, 1, 3, 2, 2, 3);

        INSERT INTO houses (id, amount_rooms, amount_bathrooms, character_id) VALUES
            (1, 2, 1, 1),
            (2, 3, 2, 2),
            (3, 1, 1, 3),
            (4, 4, 2, 4),
            (5, 2, 2, 5),
            (6, 5, 3, 6),
            (7, 3, 1, 7),
            (8, 2, 1, 8),
            (9, 4, 3, 9),
            (10, 1, 1, 10);

    ELSE
        RAISE EXCEPTION 'Unexpected schema: expected exactly one FK path between houses and characters.';
    END IF;
END $$;

INSERT INTO gang_affiliations (character_id, gang_id, join_date) VALUES
    (1, 2, '2025-01-12'),
    (2, 1, '2025-02-03'),
    (3, 7, '2025-03-22'),
    (4, 5, '2025-04-10'),
    (6, 6, '2025-06-16'),
    (9, 10, '2025-09-25');

COMMIT;
