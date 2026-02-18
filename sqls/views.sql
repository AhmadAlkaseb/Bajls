CREATE OR REPLACE VIEW v_character_overview AS

SELECT

profiles.username AS username,
characters.name AS name,
characters.balance AS balance,
COALESCE(STRING_AGG(gangs.name, ', '), 'Spineless') AS affiliations

FROM characters

JOIN profiles ON profiles.id = characters.profile_id
LEFT JOIN gang_affiliations ON gang_affiliations.character_id = characters.id
LEFT JOIN gangs ON gangs.id = gang_affiliations.gang_id

GROUP BY

profiles.username,
characters.name,
characters.balance;






CREATE OR REPLACE VIEW v_gang_overview AS

SELECT

gangs.name AS gang_name,
COALESCE(STRING_AGG(characters.name, ', '), 'No members') AS members

FROM gangs

LEFT JOIN gang_affiliations ON gang_affiliations.gang_id = gangs.id
LEFT JOIN characters ON characters.id = gang_affiliations.character_id

GROUP BY

gangs.name;






CREATE OR REPLACE VIEW v_character_appearance AS

SELECT

characters.name AS character_name,
characters.balance AS balance,
genders.name AS gender,
weights.name AS weight,
heights.name AS height,
eyecolors.name AS eye_color,
skincolors.name AS skin_color

FROM characters

JOIN genders ON genders.id = characters.gender_id
JOIN weights ON weights.id = characters.weight_id
JOIN heights ON heights.id = characters.height_id
JOIN eyecolors ON eyecolors.id = characters.eyecolor_id
JOIN skincolors ON skincolors.id = characters.skincolor_id;