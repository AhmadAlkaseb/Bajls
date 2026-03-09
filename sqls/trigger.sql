CREATE OR REPLACE FUNCTION fn_character_notice()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE NOTICE 'Welcome! Your new character created: name=%', NEW.name;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_character_notice ON characters;

CREATE TRIGGER trg_character_notice
AFTER INSERT ON characters
FOR EACH ROW
EXECUTE FUNCTION fn_character_notice();

CREATE OR REPLACE FUNCTION fn_prevent_audit_log_changes()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'audit_log is append-only and cannot be %', TG_OP;
END;
$$;

DROP TRIGGER IF EXISTS trg_prevent_audit_log_update ON audit_log;
CREATE TRIGGER trg_prevent_audit_log_update
BEFORE UPDATE ON audit_log
FOR EACH ROW
EXECUTE FUNCTION fn_prevent_audit_log_changes();

DROP TRIGGER IF EXISTS trg_prevent_audit_log_delete ON audit_log;
CREATE TRIGGER trg_prevent_audit_log_delete
BEFORE DELETE ON audit_log
FOR EACH ROW
EXECUTE FUNCTION fn_prevent_audit_log_changes();
