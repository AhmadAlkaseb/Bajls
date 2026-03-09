BEGIN;

TRUNCATE TABLE
    gang_affiliations,
    character_quest,
    quests,
    character_drug,
    drugs,
    vehicles,
    characters,
    garages,
    houses,
    gangs,
    profiles
RESTART IDENTITY CASCADE;

INSERT INTO profiles (first_name, last_name, email, username, password, role) VALUES
    ('Mia', 'Hansen', 'mia@bajls.dev', 'mihansen', 'P@ssMia1', 'USER'),
    ('Noah', 'Larsen', 'noah@bajls.dev', 'nlarsen', 'P@ssNoah2', 'USER'),
    ('Emma', 'Nielsen', 'emma@bajls.dev', 'enielsen', 'P@ssEmma3', 'USER'),
    ('Lucas', 'Pedersen', 'lucas@bajls.dev', 'lpedersen', 'P@ssLucas4', 'USER'),
    ('Asta', 'Andersen', 'asta@bajls.dev', 'aandersen', 'P@ssAsta5', 'ADMIN');

INSERT INTO houses (amount_rooms, amount_bathrooms) VALUES
    (2, 1),
    (3, 2),
    (1, 1),
    (4, 2),
    (5, 3);

INSERT INTO garages (capacity) VALUES
    (2),
    (3),
    (1),
    (4),
    (2);

INSERT INTO characters (
    profile_id, house_id, garage_id, name, balance, gender, skincolor, eyecolor, height, weight
) VALUES
    (1, 1, 1, 'ShadowMia', 2450.50, 'FEMALE', 'LIGHT', 'BLUE', 'AVERAGE', 'LIGHT'),
    (2, 2, 2, 'SteelNoah', 1320.00, 'MALE', 'MEDIUM', 'BROWN', 'TALL', 'HEAVY'),
    (3, 3, 3, 'RuneEmma', 3890.75, 'FEMALE', 'MEDIUM', 'GREEN', 'AVERAGE', 'LIGHT'),
    (4, 4, 4, 'VoltLucas', 980.25, 'MALE', 'DARK', 'GRAY', 'SHORT', 'AVERAGE'),
    (5, 5, 5, 'AdminAsta', 5000.00, 'NON_BINARY', 'MEDIUM', 'BROWN', 'TALL', 'AVERAGE');

INSERT INTO vehicles (garage_id, model, type, plate_number) VALUES
    (1, 'Sultan RS', 'CAR', 'BAJLS-101'),
    (1, 'Bati 801', 'MOTORCYCLE', 'BAJLS-102'),
    (2, 'Sandking XL', 'TRUCK', 'BAJLS-201'),
    (4, 'Rumpo', 'VAN', 'BAJLS-401'),
    (5, 'Kuruma', 'CAR', 'BAJLS-501');

INSERT INTO drugs (name, type) VALUES
    ('Blue Dream', 'WEED'),
    ('Snowfall', 'COCAINE'),
    ('Night Glass', 'METH'),
    ('Red Calm', 'PILLS');

INSERT INTO character_drug (character_id, drug_id, quantity) VALUES
    (1, 1, 5),
    (1, 4, 2),
    (2, 2, 3),
    (3, 1, 1),
    (4, 3, 4);

INSERT INTO quests (title, description, reward) VALUES
    ('First Delivery', 'Deliver a package across the city without losing it.', 500.00),
    ('Garage Upgrade', 'Earn enough money to expand your garage.', 1200.00),
    ('Gang Contact', 'Meet a gang contact and complete the introduction task.', 900.00),
    ('Street Sweep', 'Control a district for one in-game day.', 1500.00);

INSERT INTO character_quest (character_id, quest_id, status, accepted_at) VALUES
    (1, 1, 'COMPLETED', '2026-03-01 10:00:00'),
    (1, 2, 'IN_PROGRESS', '2026-03-05 14:15:00'),
    (2, 3, 'ACCEPTED', '2026-03-06 09:30:00'),
    (3, 1, 'FAILED', '2026-03-02 18:20:00'),
    (5, 4, 'AVAILABLE', '2026-03-07 12:00:00');

INSERT INTO gangs (name, type) VALUES
    ('Iron Wolves', 'STREET'),
    ('Neon Foxes', 'MAFIA'),
    ('Crimson Tide', 'CARTEL'),
    ('Night Owls', 'BIKER');

INSERT INTO gang_affiliations (character_id, gang_id, join_date) VALUES
    (1, 2, '2026-01-12'),
    (2, 1, '2026-02-03'),
    (3, 4, '2026-03-01'),
    (4, 3, '2026-02-10'),
    (5, 2, '2026-01-20');

COMMIT;
