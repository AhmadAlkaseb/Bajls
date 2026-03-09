CREATE OR REPLACE VIEW v_character_overview AS
SELECT
    p.username,
    c.name AS character_name,
    c.balance,
    h.amount_rooms,
    h.amount_bathrooms,
    g.capacity AS garage_capacity,
    COALESCE(STRING_AGG(DISTINCT ga2.name, ', '), 'No gang') AS affiliations
FROM characters c
JOIN profiles p ON p.id = c.profile_id
JOIN houses h ON h.id = c.house_id
JOIN garages g ON g.id = c.garage_id
LEFT JOIN gang_affiliations gaf ON gaf.character_id = c.id
LEFT JOIN gangs ga2 ON ga2.id = gaf.gang_id
GROUP BY p.username, c.name, c.balance, h.amount_rooms, h.amount_bathrooms, g.capacity;

CREATE OR REPLACE VIEW v_character_appearance AS
SELECT
    c.name AS character_name,
    c.gender,
    c.skincolor,
    c.eyecolor,
    c.height,
    c.weight
FROM characters c;

CREATE OR REPLACE VIEW v_gang_overview AS
SELECT
    g.name AS gang_name,
    g.type AS gang_type,
    COALESCE(STRING_AGG(c.name, ', '), 'No members') AS members
FROM gangs g
LEFT JOIN gang_affiliations ga ON ga.gang_id = g.id
LEFT JOIN characters c ON c.id = ga.character_id
GROUP BY g.name, g.type;

CREATE OR REPLACE VIEW v_character_assets AS
SELECT
    c.name AS character_name,
    COUNT(DISTINCT v.id) AS vehicle_count,
    COUNT(DISTINCT cd.drug_id) AS drug_types,
    COUNT(DISTINCT cq.quest_id) AS quest_count
FROM characters c
LEFT JOIN garages g ON g.id = c.garage_id
LEFT JOIN vehicles v ON v.garage_id = g.id
LEFT JOIN character_drug cd ON cd.character_id = c.id
LEFT JOIN character_quest cq ON cq.character_id = c.id
GROUP BY c.name;
