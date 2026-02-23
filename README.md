# Project Bajls by

**Ahmad**  
cph-aa540@stud.ek.dk    
@AhmadAlkaseb

**Sadek**  
saal1004@stud.ek.dk  
@sadekkk12  

**Benjamin**  
cph-bh226@stud.ek.dk  
@BenjaminHernandez95  

**Jeppe**  
cph-jk469@stud.ek.dk  
@JeppeKoch  

**Laith**  
cph-la356@stud.ek.dk  
@Mingo-inc

**How to:**  
Make sure you have created the following name database: **bajls**.
And added the following environment variables:  
**DB_URL** = jdbc:postgresql://localhost:5432/bajls  
**DB_USER** = postgres  
**DB_PASSWORD** = postgres  
**DB_NAME** = bajls

## Docker setup (Postgres + pgAdmin + pg_cron)

### 1) Prerequisites
- Docker Desktop installed and running.
- Files present in project root:
  - `Dockerfile` (installs `postgresql-15-cron`)
  - `docker-compose.yml`

### 2) Start containers
Run in project root:

```bash
docker compose up -d --build
```

This starts:
- `db_2sem` (PostgreSQL on host port `5432`)
- `pgadmin_2sem` (pgAdmin on `http://localhost:8080`)
- `mongodb_2sem` (MongoDB on host port `27017`)

### MongoDB quick start (single command)
If you only need MongoDB, run:

```bash
docker compose up -d mongodb
```

MongoDB connection values:
- Host: `localhost`
- Port: `27017`
- Username: `root`
- Password: `root`
- Database: `bajls`

Connection string:

```text
mongodb://root:root@localhost:27017/bajls?authSource=admin
```

### 3) Login to pgAdmin
- URL: `http://localhost:8080`
- Email: `admin@cphbusiness.dk`
- Password: `1234`

### 4) Register database server in pgAdmin
Use these connection values:
- Host: `db`
- Port: `5432`
- Maintenance DB: `postgres` (or `bajls`)
- Username: `postgres`
- Password: `postgres`

Note: Use `db` as host (not `localhost`) because pgAdmin runs in Docker on the same network.

### 5) Enable and verify pg_cron
Run in Query Tool:

```sql
CREATE EXTENSION IF NOT EXISTS pg_cron;
SHOW shared_preload_libraries;
```

`shared_preload_libraries` must include `pg_cron`.

### 6) Create daily loyalty cron job
Run:

```sql
\i sqls/daily_loyalty_bonus.sql
```

If `\i` is not supported in your pgAdmin context, copy/paste the SQL from:
- `sqls/daily_loyalty_bonus.sql`

### 7) Verify job exists
```sql
SELECT jobid, jobname, schedule, active
FROM cron.job
WHERE jobname = 'daily_loyalty_bonus';
```
