-- Security bootstrap: database users/privileges (least privilege)
-- Change these passwords before production use.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'bajls_readonly') THEN
        CREATE ROLE bajls_readonly NOLOGIN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'bajls_readwrite') THEN
        CREATE ROLE bajls_readwrite NOLOGIN;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'bajls_app_user') THEN
        CREATE ROLE bajls_app_user LOGIN PASSWORD 'change_me_user';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'bajls_app_admin') THEN
        CREATE ROLE bajls_app_admin LOGIN PASSWORD 'change_me_admin';
    END IF;
END $$;

-- Role membership
GRANT bajls_readonly TO bajls_app_user;
GRANT bajls_readwrite TO bajls_app_admin;

-- Basic database/schema access
GRANT CONNECT ON DATABASE bajls TO bajls_app_user, bajls_app_admin;
GRANT USAGE ON SCHEMA public TO bajls_readonly, bajls_readwrite;

-- Existing objects
GRANT SELECT ON ALL TABLES IN SCHEMA public TO bajls_readonly;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO bajls_readwrite;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO bajls_readwrite;

-- Future objects created by postgres (owner in this project)
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
    GRANT SELECT ON TABLES TO bajls_readonly;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bajls_readwrite;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO bajls_readwrite;
