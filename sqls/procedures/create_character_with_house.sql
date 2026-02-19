CREATE OR REPLACE PROCEDURE create_character_with_house(
    IN p_name VARCHAR(20),
    IN p_balance NUMERIC,
    IN p_profile_id INT,
    IN p_gender_id INT,
    IN p_skincolor_id INT,
    IN p_eyecolor_id INT,
    IN p_height_id INT,
    IN p_weight_id INT,
    IN p_rooms INT,
    IN p_bathrooms INT
)
LANGUAGE plpgsql
AS $$
DECLARE
v_house_id INT;
BEGIN

	-- We set it to the current max id so that the next insert will use the next sequential id.
	PERFORM setval('houses_id_seq', (SELECT COALESCE(MAX(id), 0) FROM houses));


    -- Create house first
INSERT INTO houses (amount_rooms, amount_bathrooms)
VALUES (p_rooms, p_bathrooms)
    RETURNING id INTO v_house_id;

-- Create character linked to that house
INSERT INTO characters (
    name, balance, profile_id, gender_id, skincolor_id, eyecolor_id, height_id, weight_id, house_id
)
VALUES (
           p_name, p_balance, p_profile_id, p_gender_id, p_skincolor_id, p_eyecolor_id, p_height_id, p_weight_id, v_house_id
       );
END;
$$;
