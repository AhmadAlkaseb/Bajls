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

## Setup

Opret databasen `bajls` og tilføj disse miljøvariabler:

- `DB_URL=jdbc:postgresql://localhost:5432/bajls`
- `DB_USER=postgres`
- `DB_PASSWORD=postgres`
- `DB_NAME=bajls`
- `DB_URL=jdbc:postgresql://localhost:5432/bajls;DB_USER=postgres;DB_PASSWORD=postgres;DB_NAME=bajls`

## Docker

Start containere fra projektroden:

```bash
docker compose up -d --build
```

Det starter:

- PostgreSQL på `localhost:5432`
- pgAdmin på `http://localhost:8080`
- MongoDB på `localhost:27017`

Kun MongoDB:

```bash
docker compose up -d mongodb
```

```text
mongodb://root:root@localhost:27017/bajls?authSource=admin
```

## pgAdmin

- URL: `http://localhost:8080`
- Email: `admin@cphbusiness.dk`
- Password: `1234`
- Host: `db`
- Port: `5432`
- Username: `postgres`
- Password: `postgres`

## pg_cron

Kør i Query Tool:

```sql
CREATE EXTENSION IF NOT EXISTS pg_cron;
\i sqls/daily_loyalty_bonus.sql
```
