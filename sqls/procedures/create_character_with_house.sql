CREATE OR REPLACE PROCEDURE create_character_with_house(
    IN p_name varchar(50),
    IN p_balance numeric(12,2),
    IN p_profile_id bigint,
    IN p_gender varchar(30),
    IN p_skincolor varchar(30),
    IN p_eyecolor varchar(30),
    IN p_height varchar(30),
    IN p_weight varchar(30),
    IN p_rooms integer,
    IN p_bathrooms integer,
    IN p_garage_capacity integer
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_house_id bigint;
    v_garage_id bigint;
BEGIN
    INSERT INTO houses (amount_rooms, amount_bathrooms)
    VALUES (p_rooms, p_bathrooms)
    RETURNING id INTO v_house_id;

    INSERT INTO garages (capacity)
    VALUES (p_garage_capacity)
    RETURNING id INTO v_garage_id;

    INSERT INTO characters (
        name, balance, profile_id, gender, skincolor, eyecolor, height, weight, house_id, garage_id
    )
    VALUES (
        p_name, p_balance, p_profile_id, p_gender, p_skincolor, p_eyecolor, p_height, p_weight, v_house_id, v_garage_id
    );
END;
$$;
