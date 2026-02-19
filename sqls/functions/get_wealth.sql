-- This function calculates wealth status based on player balance
CREATE OR REPLACE FUNCTION get_wealth_status(p_balance numeric)
RETURNS text
LANGUAGE plpgsql
AS $$
BEGIN
    IF p_balance < 1000 THEN
        RETURN 'poor';
    ELSIF p_balance < 3000 THEN
        RETURN 'middleclass';
ELSE
        RETURN 'rich';
END IF;
END;
$$;