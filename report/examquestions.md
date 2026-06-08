# Exam Questions

## 1. Performance & Optimization

### Explain

Performance handler om, hvor effektivt et databasesystem og den tilhørende backend kan behandle arbejde. Det betyder ikke kun, at en enkelt query skal være hurtig. Det handler også om, hvor mange brugere systemet kan håndtere samtidig, hvor stabil svartiden er, og hvor godt systemet udnytter CPU, hukommelse, disk og netværk.

I Bajls-projektet kan performance vurderes på tværs af tre databaser:

- PostgreSQL bruges som den relationelle source of truth, hvor integritet, constraints og transaktioner er vigtige.
- MongoDB bruges til dokumentbaserede profile aggregates, hvor en hel profil med characters og relaterede data kan læses samlet.
- Neo4j bruges til relationship-heavy queries, hvor traversal mellem characters, gangs, quests og ownership er centralt.

Der er især to centrale performance-begreber:

- **Latency**: hvor lang tid én operation tager, for eksempel hvor hurtigt API'et kan hente én character.
- **Throughput**: hvor mange operationer systemet kan håndtere pr. tidsenhed, for eksempel hvor mange profile reads pr. sekund systemet kan levere.

En database kan være optimeret til lav latency for enkelte forespørgsler, men stadig have lav throughput under mange samtidige brugere. Omvendt kan et system håndtere mange requests, men med højere svartid for hver enkelt request.

Query optimization betyder at forbedre, hvordan en query bliver udført. Databasen laver normalt en execution plan, hvor den beslutter, om den skal bruge index scan, sequential scan, nested loop join, hash join, sortering, filtering osv. Execution plans genereres af databasen, fordi SQL beskriver *hvad* man vil hente, ikke præcist *hvordan* det skal hentes.

En execution plan viser typisk:

- hvilke tabeller der læses
- om der bruges index eller full table scan
- join-strategi
- estimeret og faktisk antal rækker
- sortering og filtrering
- cost-estimater
- faktisk tid, hvis man bruger `EXPLAIN ANALYZE`

Execution plans hjælper designet, fordi de afslører, om schema, indexes og query patterns passer sammen. Hvis Bajls ofte søger characters ud fra `profile_id`, bør der være et index på den foreign key. Hvis gang-medlemskaber ofte findes via `character_id` eller `gang_id`, bør junction-tabellen understøtte det med indexes.

Index usage er en af de vigtigste performance-teknikker. Et index gør opslag hurtigere, men det er ikke gratis. Indexes fylder på disk, skal opdateres ved inserts/updates/deletes, og for mange indexes kan gøre write-heavy workloads langsommere.

Concurrent access og locking handler om, at flere brugere eller processer arbejder med data samtidig. Databasen skal sikre isolation, så to transaktioner ikke ødelægger hinandens data. I PostgreSQL håndteres dette med MVCC og locks. Ved almindelige reads blokerer brugere ofte ikke hinanden, men writes kan stadig låse rækker eller skabe konflikter.

N+1-problemet opstår i backend, når man først henter én liste og derefter laver én ekstra query pr. element. Hvis systemet henter 50 profiles og derefter laver 50 separate queries for characters, bliver det 51 queries. Det er ofte meget langsommere end én join-query, en batch query eller en fetch strategy.

Caching betyder at gemme ofte brugte resultater tættere på applikationen eller brugeren. Det kan være:

- in-memory cache i backend
- HTTP cache
- database query cache eller prepared statements
- Redis-lignende ekstern cache
- MongoDB aggregate documents som en form for read-optimized projection

Read-heavy workloads optimeres ofte med indexes, caching, denormalisering, materialized views eller read replicas. Write-heavy workloads optimeres ved at begrænse antallet af indexes, holde transaktioner korte, batch-inserte data og undgå unødvendige constraints eller triggers i hot paths.

OLTP og OLAP er to forskellige typer workloads:

- **OLTP**: mange små, samtidige transaktioner, for eksempel oprette character, opdatere balance eller acceptere quest.
- **OLAP**: analytiske queries over store datamængder, for eksempel rapporter over mest aktive gangs eller gennemsnitlig wealth pr. character type.

Performance skal måles, ikke gættes. Man kan måle svartid, throughput, CPU, memory, disk I/O, lock waits, query duration, index hit ratio og slow queries.

### Compare

Latency-optimering og throughput-optimering kan trække i forskellige retninger. Hvis man optimerer én bestemt query med et ekstra index, kan latency falde for den query, men write-throughput kan falde, fordi indexet skal vedligeholdes. Hvis man batcher writes for højere throughput, kan den enkelte bruger opleve højere latency, fordi data først skrives samlet.

PostgreSQL, MongoDB og Neo4j har også forskellige performance trade-offs i Bajls-projektet:

- PostgreSQL er stærk til konsistente transaktioner, joins og constraints, men komplekse joins kan blive dyre uden gode indexes.
- MongoDB er stærk til at læse et helt profile aggregate hurtigt, men denormalisering kan give større dokumenter og sværere konsistens på tværs af collections.
- Neo4j er stærk til relationship traversal, for eksempel characters connected to gangs and quests, men simple tabulære rapporter kan være mere naturlige i PostgreSQL.

Index usage vs misuse:

- Et godt index matcher et konkret query pattern, for eksempel `characters(profile_id)`.
- Et dårligt index bliver sjældent brugt, dublerer et andet index eller gør writes langsommere uden at forbedre reads.
- Et index på en kolonne med meget lav selektivitet, for eksempel en boolean med næsten samme værdi for alle rækker, hjælper ofte mindre end forventet.

Caching giver hurtige reads, men introducerer cache invalidation. Hvis en character opdateres i PostgreSQL, men en cache stadig har den gamle version, kan brugeren se stale data. Derfor passer caching bedst til data, der enten ændrer sig sjældent, eller hvor lidt forsinkelse er acceptabelt.

Read-heavy og write-heavy workloads kræver forskellig datamodel:

- Read-heavy: denormalisering, embedded documents, materialized views og flere indexes kan give mening.
- Write-heavy: normalisering, færre indexes, korte transaktioner og mindre duplication er ofte bedre.

OLTP og OLAP skal normalt ikke optimeres på samme måde. OLTP handler om korte, sikre operationer. OLAP handler om store scans, aggregations og historiske analyser. Hvis man blander tung OLAP direkte ind i en OLTP-produktionsdatabase, kan rapporter gøre gameplay-operationer langsommere.

### Demonstrate

Eksempel på query optimization i PostgreSQL:

```sql
-- Find all characters for one profile
SELECT id, name, level, balance
FROM characters
WHERE profile_id = 10;
```

Hvis tabellen er stor, bør databasen kunne bruge et index:

```sql
CREATE INDEX idx_characters_profile_id
ON characters(profile_id);
```

Execution plan:

```sql
EXPLAIN ANALYZE
SELECT id, name, level, balance
FROM characters
WHERE profile_id = 10;
```

Et godt tegn kan være, at planen bruger `Index Scan` eller `Bitmap Index Scan` på `idx_characters_profile_id`, især hvis kun få rækker matcher.

Eksempel på join i Bajls-domænet:

```sql
EXPLAIN ANALYZE
SELECT c.id, c.name, g.name AS gang_name, ga.join_date
FROM characters c
JOIN gang_affiliations ga ON ga.character_id = c.id
JOIN gangs g ON g.id = ga.gang_id
WHERE c.id = 42;
```

Relevante indexes:

```sql
CREATE INDEX idx_gang_affiliations_character_id
ON gang_affiliations(character_id);

CREATE INDEX idx_gang_affiliations_gang_id
ON gang_affiliations(gang_id);
```

Eksempel på N+1-problem i pseudo-code:

```text
profiles = profileRepository.findAll()

for profile in profiles:
    characters = characterRepository.findByProfileId(profile.id)
```

Hvis der er 100 profiles, giver det 1 query for profiles + 100 queries for characters.

Bedre løsning:

```sql
SELECT p.id AS profile_id,
       p.username,
       c.id AS character_id,
       c.name AS character_name
FROM profiles p
LEFT JOIN characters c ON c.profile_id = p.id;
```

Eller backend pseudo-code med batch loading:

```text
profiles = profileRepository.findAll()
profileIds = profiles.map(p -> p.id)
charactersByProfile = characterRepository.findByProfileIds(profileIds)
attachCharacters(profiles, charactersByProfile)
```

Eksempel på caching:

```text
function getProfileAggregate(profileId):
    cacheKey = "profile:" + profileId

    cached = cache.get(cacheKey)
    if cached exists:
        return cached

    profile = database.loadProfileAggregate(profileId)
    cache.set(cacheKey, profile, ttl = 60 seconds)
    return profile
```

Eksempel på read-heavy optimization i MongoDB:

```javascript
db.profiles.findOne(
  { id: 10 },
  { username: 1, characters: 1, houses: 1, garages: 1 }
)
```

Her kan ét dokument give en samlet profile view, hvor PostgreSQL muligvis skal bruge flere joins.

Eksempel på Neo4j traversal:

```cypher
MATCH (c:Character {id: 42})-[:MEMBER_OF]->(g:Gang)
RETURN c.name, g.name;
```

Denne type query er naturlig i Neo4j, fordi relationen er direkte modelleret som en edge i stedet for en junction table.

Eksempel på performance measurement:

```sql
EXPLAIN (ANALYZE, BUFFERS)
SELECT c.id, c.name, g.name
FROM characters c
JOIN gang_affiliations ga ON ga.character_id = c.id
JOIN gangs g ON g.id = ga.gang_id
WHERE g.id = 3;
```

Man kan her se både faktisk køretid og buffer usage, hvilket viser om databasen primært læser fra memory eller disk.

### Discuss

Performance optimization bør starte med konkrete mål. Det er ikke nok at sige, at databasen skal være hurtig. Man skal definere, hvad der betyder mest: lav latency for en bestemt API-route, høj throughput under mange spillere, hurtige analytics queries eller stabil performance ved samtidige writes.

I Bajls-projektet giver det mening at lade PostgreSQL være den stærke konsistente base, mens MongoDB og Neo4j fungerer som read-optimized modeller til bestemte access patterns. PostgreSQL er bedst, når systemet skal håndhæve ejerskab, mandatory relationships og transaktioner. MongoDB er bedre, når API'et ofte skal vise en samlet profilside. Neo4j er bedre, når spørgsmålene handler om netværk og relationer, for eksempel hvilke characters der er forbundet gennem gangs eller quests.

Den vigtigste åbne overvejelse er, at optimization altid har en pris. Flere indexes kan forbedre reads, men gøre writes dyrere. Caching kan gøre systemet hurtigt, men give stale data. Denormalisering kan reducere joins, men øge risikoen for duplication og inkonsistens. Locking og transaktioner beskytter data, men kan reducere concurrency, hvis transaktionerne er lange.

Derfor bør performance arbejdet følge denne rækkefølge:

1. Mål den nuværende performance.
2. Find de langsomme queries eller API-routes.
3. Undersøg execution plans.
4. Tilføj eller ændr indexes ud fra konkrete query patterns.
5. Ret backend-problemer som N+1.
6. Overvej caching eller alternative datamodeller.
7. Mål igen for at sikre, at ændringen faktisk hjalp.

Et godt eksamenssvar er derfor ikke bare at nævne indexes, men at forklare, at performance afhænger af workload, datamodel, query pattern, concurrency og måling. Optimization er en balance mellem hurtige reads, sikre writes, korrekt data og en arkitektur, der passer til systemets faktiske brug.

## 2. Database Indexes

### Explain

Et database index er en datastruktur, som gør det hurtigere at finde rækker uden at scanne hele tabellen. Man kan sammenligne det med et register i en bog: i stedet for at læse alle sider finder man hurtigt den relevante reference og går direkte til siden.

I PostgreSQL er den mest almindelige indextype et B-tree index. Det holder værdier sorteret, så databasen hurtigt kan finde rows med `=`, range queries som `<` og `>`, og sortering med `ORDER BY`. Når databasen bruger et index, slår den først op i indexet og finder derefter de relevante rows i tabellen.

Eksempel: hvis vi vil finde en user/profile ud fra last name, og tabellen har mange rows, kan et index på `last_name` gøre opslaget hurtigere.

I Bajls-projektet kan samme princip bruges på for eksempel:

- `profiles(username)` til login eller profilopslag
- `characters(profile_id)` til at finde alle characters for én profile
- `gang_affiliations(character_id)` til at finde en characters gang memberships
- `vehicles(garage_id)` til at finde vehicles i en garage

Hvis man har flere `WHERE` statements, kan man enten bruge flere enkeltkolonne-indexes eller et composite index. Et composite index er et index på flere kolonner i en bestemt rækkefølge.

Eksempel:

```sql
CREATE INDEX idx_characters_profile_level
ON characters(profile_id, level);
```

Det index er særligt relevant for queries som:

```sql
SELECT *
FROM characters
WHERE profile_id = 10
  AND level >= 5;
```

Rækkefølgen i et composite index betyder noget. Et index på `(profile_id, level)` er godt, når queryen filtrerer på `profile_id`, eventuelt sammen med `level`. Det er mindre brugbart, hvis queryen kun filtrerer på `level`, fordi `level` ikke er den første kolonne i indexet.

Et covering index er et index, der indeholder alle de kolonner, queryen skal bruge. Så kan databasen nogle gange besvare queryen direkte fra indexet uden at slå op i selve tabellen. I PostgreSQL kan man bruge `INCLUDE` til ekstra kolonner, der ikke indgår i søgenøglen.

Index selectivity handler om, hvor meget et index reducerer antallet af rows. Høj selectivity betyder, at en filterværdi matcher få rows. Lav selectivity betyder, at mange rows matcher. Column cardinality handler om, hvor mange forskellige værdier en kolonne har.

Eksempel:

- `username` har høj cardinality, fordi mange brugere har unikke usernames.
- `role` har lav cardinality, hvis værdierne kun er `ADMIN` og `USER`.

Indexes er mest nyttige på kolonner med høj selectivity og på kolonner, der ofte bruges i `WHERE`, `JOIN`, `ORDER BY` eller `GROUP BY`.

### Compare

Fordele ved indexes:

- Hurtigere reads og lookups
- Hurtigere joins, hvis foreign keys og join-kolonner er indekseret
- Hurtigere sortering og filtering
- Kan hjælpe uniqueness, for eksempel unique index på `username`
- Covering indexes kan reducere tabelopslag

Ulemper ved indexes:

- Bruger ekstra diskplads
- Gør inserts, updates og deletes langsommere
- Skal vedligeholdes af databasen
- Kan være spild, hvis de sjældent bruges
- For mange indexes kan gøre optimizerens valg mere komplekse

Typiske indextyper:

- **B-tree index**: standardvalg til equality, ranges og sorting.
- **Hash index**: bruges til equality lookups, men B-tree er ofte mere fleksibelt.
- **GIN index**: godt til arrays, JSONB og full-text-lignende opslag i PostgreSQL.
- **GiST index**: bruges ofte til geometriske data, ranges og mere specialiserede søgninger.
- **Composite index**: index på flere kolonner.
- **Unique index**: sikrer at værdier er unikke.
- **Partial index**: index på et subset af rows.
- **Covering index**: indeholder alle kolonner, queryen skal bruge.

Composite index vs flere single-column indexes:

- Composite index er bedst, når queries ofte filtrerer på de samme kolonner sammen.
- Single-column indexes er mere fleksible, hvis kolonnerne bruges i mange forskellige queries.
- Rækkefølgen i composite indexes skal passe til queryens mest selektive og mest brugte filtermønstre.

Index usage vs full table scan:

- Hvis queryen henter få rows, er index ofte godt.
- Hvis queryen henter næsten hele tabellen, kan sequential scan være hurtigere.
- Databasen vælger selv ud fra statistik, cost og row estimates.

### Demonstrate

Simpelt index på last name:

```sql
CREATE INDEX idx_profiles_last_name
ON profiles(last_name);

SELECT *
FROM profiles
WHERE last_name = 'Hansen';
```

Hvis queryen ofte bruger både first name og last name:

```sql
CREATE INDEX idx_profiles_last_first
ON profiles(last_name, first_name);

SELECT *
FROM profiles
WHERE last_name = 'Hansen'
  AND first_name = 'Jeppe';
```

Eksempel fra Bajls: find characters for en profile:

```sql
CREATE INDEX idx_characters_profile_id
ON characters(profile_id);

SELECT id, name, level
FROM characters
WHERE profile_id = 10;
```

Composite index med multiple `WHERE` conditions:

```sql
CREATE INDEX idx_characters_profile_status
ON characters(profile_id, status);

SELECT id, name
FROM characters
WHERE profile_id = 10
  AND status = 'ACTIVE';
```

Covering index:

```sql
CREATE INDEX idx_characters_profile_covering
ON characters(profile_id)
INCLUDE (id, name, level);

SELECT id, name, level
FROM characters
WHERE profile_id = 10;
```

Her kan indexet potentielt dække hele queryen, fordi `profile_id` bruges til filtering, og `id`, `name`, `level` findes i indexet.

Partial index:

```sql
CREATE INDEX idx_active_characters_profile
ON characters(profile_id)
WHERE status = 'ACTIVE';
```

Det er kun relevant, hvis systemet ofte spørger efter aktive characters:

```sql
SELECT *
FROM characters
WHERE profile_id = 10
  AND status = 'ACTIVE';
```

Eksempel på execution plan check:

```sql
EXPLAIN ANALYZE
SELECT id, name, level
FROM characters
WHERE profile_id = 10;
```

Hvis planen viser `Seq Scan`, betyder det, at databasen scanner tabellen. Hvis planen viser `Index Scan`, bruger den indexet.

Typiske situationer hvor et index ikke bruges:

1. Queryen bruger en funktion på kolonnen:

```sql
SELECT *
FROM profiles
WHERE LOWER(username) = 'ahmad';
```

Et normalt index på `username` bruges ikke nødvendigvis, fordi værdien transformeres. Fix:

```sql
CREATE INDEX idx_profiles_lower_username
ON profiles(LOWER(username));
```

2. Queryen bruger wildcard først i `LIKE`:

```sql
SELECT *
FROM profiles
WHERE username LIKE '%mad';
```

Et B-tree index hjælper normalt ikke godt her, fordi søgningen ikke starter fra begyndelsen af værdien. Fix kan være full-text search, trigram index eller ændret search pattern:

```sql
SELECT *
FROM profiles
WHERE username LIKE 'ahm%';
```

3. Queryen henter for mange rows:

```sql
SELECT *
FROM profiles
WHERE role = 'USER';
```

Hvis næsten alle profiles har role `USER`, er selectivity lav, og sequential scan kan være bedre. Fix er enten ikke at indeksere kolonnen alene eller bruge et mere selektivt composite/partial index.

4. Datatyper matcher ikke:

```sql
SELECT *
FROM characters
WHERE id::text = '42';
```

Casting på kolonnen kan forhindre normal index usage. Fix:

```sql
SELECT *
FROM characters
WHERE id = 42;
```

5. Composite index bruges med forkert venstre-prefix:

```sql
CREATE INDEX idx_characters_profile_level
ON characters(profile_id, level);

-- Mindre effektivt for dette index, fordi profile_id ikke bruges
SELECT *
FROM characters
WHERE level = 5;
```

Fix:

```sql
CREATE INDEX idx_characters_level
ON characters(level);
```

eller design et composite index, der passer til de faktiske queries.

### Discuss

Man skal ikke tilføje indexes automatisk på alle kolonner. Et index bør tilføjes, når der findes et konkret query pattern, og når måling eller execution plans viser, at det hjælper.

I Bajls-projektet er indexes mest oplagte på primary keys, foreign keys, login-felter og relationstabeller. For eksempel giver indexes på `characters(profile_id)` og `gang_affiliations(character_id, gang_id)` mening, fordi projektet ofte navigerer mellem profiles, characters og gangs.

Man bør derimod være forsigtig med indexes på kolonner med lav cardinality, sjældent brugte kolonner eller kolonner der ofte opdateres. Hvis en kolonne ændres hele tiden, skal indexet også opdateres hele tiden. Det kan skade write performance.

Man bør typisk ikke tilføje et index når:

- tabellen er meget lille
- kolonnen næsten aldrig bruges i filtering, joining eller sorting
- kolonnen har meget lav selectivity
- workloaden er meget write-heavy
- et eksisterende composite index allerede dækker queryen
- indexet kun hjælper en sjælden query, men gør hyppige writes langsommere

Den bedste tilgang er at starte fra de vigtigste queries, bruge `EXPLAIN ANALYZE`, vurdere selectivity og derefter tilføje indexes målrettet. Indexes er ikke bare en generel performance-knap. De er en designbeslutning, hvor man bytter hurtigere reads for ekstra storage og langsommere writes.

## 3. Caching

### Explain

Caching betyder, at man gemmer data midlertidigt et sted, hvor det er hurtigere at hente end fra den oprindelige database. Formålet er at reducere latency, aflaste databasen og øge throughput.

Der er forskel på database cache og application cache:

- **Database cache** ligger inde i databasesystemet. PostgreSQL og operativsystemet cacher ofte disk pages i memory, så gentagne queries kan læses hurtigere.
- **Application cache** ligger i eller tæt på backend-applikationen, for eksempel som in-memory cache, HTTP cache eller en ekstern cache som Redis.

I Bajls-projektet kunne application caching bruges til ofte læste profile views, character overviews eller catalog data som quests, drugs og gangs. De data kan være dyre at samle fra flere tabeller i PostgreSQL, men hurtige at returnere fra cache.

Cache invalidation er problemet med at holde cache korrekt, når den oprindelige data ændrer sig. Hvis en character opdateres i databasen, men cachen stadig indeholder den gamle version, kan API'et returnere stale data. Derfor er cache invalidation ofte den sværeste del af caching.

To almindelige caching patterns er:

- **Read-through caching**: applikationen læser først fra cache. Hvis data ikke findes, hentes den fra databasen og lægges i cache.
- **Write-through caching**: writes går gennem cachelaget, som samtidig opdaterer databasen. Det giver mere konsistent cache, men kan gøre writes langsommere.

### Compare

Database cache kræver normalt mindre applikationslogik, fordi databasesystemet selv håndterer caching af pages og query execution. Til gengæld cacher den ikke nødvendigvis færdige API-responses eller komplekse aggregates.

Application cache er mere fleksibel, fordi man kan cache præcis det, API'et bruger. For eksempel kan Bajls cache en samlet profile aggregate, som ellers kræver joins eller embedded document loading. Ulempen er, at applikationen selv skal håndtere invalidation, TTL og consistency.

Read-through caching er god til read-heavy workloads, fordi den kun loader data ved cache miss. Write-through caching er bedre, hvis man vil holde cache opdateret med det samme, men den giver ekstra arbejde ved hver write.

Man kan også bruge TTL, hvor cache entries automatisk udløber efter en bestemt tid. Det er simpelt, men betyder at brugere kan se gammel data indtil TTL udløber.

### Demonstrate

Read-through cache:

```text
function getProfile(profileId):
    key = "profile:" + profileId

    cached = cache.get(key)
    if cached exists:
        return cached

    profile = database.findProfileAggregate(profileId)
    cache.set(key, profile, ttl = 60 seconds)
    return profile
```

Write-through cache:

```text
function updateCharacter(characterId, update):
    database.transaction:
        character = database.updateCharacter(characterId, update)
        profileId = character.profileId
        aggregate = database.findProfileAggregate(profileId)
        cache.set("profile:" + profileId, aggregate)

    return character
```

Cache invalidation after write:

```text
function updateBalance(characterId, newBalance):
    database.updateCharacterBalance(characterId, newBalance)
    cache.delete("character:" + characterId)
    cache.delete("profile:" + profileId)
```

SQL example for data that might be cached:

```sql
SELECT p.id,
       p.username,
       c.id AS character_id,
       c.name,
       c.level
FROM profiles p
LEFT JOIN characters c ON c.profile_id = p.id
WHERE p.id = 10;
```

This profile view could be cached because it is likely read more often than it is changed.

### Discuss

Caching should be used where data is read often, expensive to compute, and acceptable to serve slightly stale for a short time. It is less suitable for critical write paths where the newest value must always be shown immediately, such as money transfers or inventory updates.

In Bajls, caching makes most sense for profile overview pages, public character data, quest catalogs and gang lists. It should be used carefully for balances, ownership and membership changes, because those affect game rules and should remain consistent.

The central trade-off is performance vs correctness. Caching can make the system feel much faster, but every cache introduces a second copy of data. The design must clearly decide when to update, delete or expire cached data.

## 4. Database Transactions

### Explain

A transaction is a unit of work that is executed as one logical operation. It starts with `BEGIN`, performs one or more reads/writes, and ends with either `COMMIT` or `ROLLBACK`.

If the transaction commits, all changes become permanent. If it rolls back, all changes inside the transaction are undone. This is important when one business operation affects multiple tables.

In Bajls, creating a character could involve inserting into `characters`, creating related ownership rows, assigning a house or garage, and updating profile-related data. A transaction ensures that the system does not end in a half-finished state.

ACID describes the classic transaction guarantees:

- **Atomicity**: all operations happen, or none happen.
- **Consistency**: constraints and rules remain valid.
- **Isolation**: concurrent transactions do not interfere incorrectly.
- **Durability**: committed data survives crashes.

BASE is a different model often used in distributed and highly available systems:

- **Basically Available**: the system tries to remain available.
- **Soft state**: data may temporarily be inconsistent.
- **Eventual consistency**: replicas or services become consistent over time.

ACID is appropriate when correctness is more important than availability or speed, for example payments, ownership, inventory, balance updates and foreign-key-based relational data. BASE is appropriate when availability and scalability matter more, and temporary inconsistency is acceptable, for example social feeds, likes, analytics counters or cached projections.

Transactions also solve concurrency issues:

- **Dirty read**: reading uncommitted data from another transaction.
- **Non-repeatable read**: reading the same row twice and getting different values because another transaction committed an update.
- **Phantom read**: repeating a query and seeing new rows that match the condition.
- **Lost update**: two transactions update the same data and one update overwrites the other.
- **Write skew**: two transactions read overlapping data and make writes that together violate a rule.

Transaction boundaries define what work belongs together. A transaction starts before the first operation that must be atomic and ends after the last operation that must succeed or fail together. Included resources can be rows, locks, indexes, database connections, memory for transaction state and sometimes external resources if distributed transactions are involved.

### Compare

Transactions have clear advantages:

- protect data integrity
- allow rollback on errors
- make multi-step operations atomic
- prevent many concurrency bugs
- allow the application to reason about consistent states

But they also have trade-offs:

- locks can block other users
- long transactions hold resources
- higher isolation can reduce concurrency
- rollback requires database bookkeeping
- distributed transactions are complex and slower

Isolation levels control the balance between consistency and concurrency:

| Isolation level | Prevents | Still may allow |
| --- | --- | --- |
| Read Uncommitted | Very little; dirty reads may be possible in some systems | dirty reads, non-repeatable reads, phantoms |
| Read Committed | dirty reads | non-repeatable reads, phantoms |
| Repeatable Read | dirty reads, non-repeatable reads | phantoms in some databases |
| Serializable | dirty reads, non-repeatable reads, phantoms, many write skews | lowest concurrency, possible serialization failures |

PostgreSQL uses MVCC. Its `Read Committed` prevents dirty reads, and each statement sees a committed snapshot. `Repeatable Read` gives a stable snapshot for the transaction. `Serializable` gives the strongest behavior, but transactions may fail and need retrying.

RDBMS vs document database vs graph database:

- **RDBMS** transactions are very mature and strong for multi-table operations with constraints and joins.
- **Document databases** often work best when updates are inside one document/aggregate. MongoDB supports multi-document transactions, but they are more expensive than single-document atomic updates.
- **Graph databases** like Neo4j support transactional writes to nodes and relationships. They are strong when a graph update must create multiple connected relationships consistently.

Single database transactions are simpler because one server controls the whole operation. Distributed transactions across multiple databases or services require coordination, often using two-phase commit, sagas or eventual consistency. Distributed transactions are slower and more fragile because failures can happen between systems.

Long-running transactions are not recommended because they:

- hold locks for longer
- keep old row versions alive in MVCC systems
- increase memory and storage pressure
- can block schema changes or writes
- increase the chance of deadlocks and conflicts
- make rollback more expensive

Error behavior differs between database systems:

- **PostgreSQL**: after an error inside a transaction, the transaction is marked aborted. You usually must `ROLLBACK` before continuing, unless you use savepoints.
- **MySQL/InnoDB**: many statement errors roll back only the failed statement, and the transaction can often continue, depending on the error and settings.
- **SQL Server**: behavior depends on settings like `XACT_ABORT`. Some errors abort the statement, while others can make the whole transaction uncommittable.

### Demonstrate

Basic transaction:

```sql
BEGIN;

UPDATE characters
SET balance = balance - 100
WHERE id = 1;

UPDATE characters
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

If anything fails:

```sql
BEGIN;

UPDATE characters
SET balance = balance - 100
WHERE id = 1;

-- Something fails here

ROLLBACK;
```

Example from Bajls: create gang membership safely:

```sql
BEGIN;

INSERT INTO gang_affiliations (character_id, gang_id, role, join_date)
VALUES (42, 3, 'MEMBER', CURRENT_DATE);

UPDATE characters
SET status = 'ACTIVE'
WHERE id = 42;

COMMIT;
```

If the `gang_id` does not exist, the foreign key should fail and the transaction should roll back.

Isolation level:

```sql
BEGIN TRANSACTION ISOLATION LEVEL SERIALIZABLE;

SELECT balance
FROM characters
WHERE id = 1;

UPDATE characters
SET balance = balance - 100
WHERE id = 1;

COMMIT;
```

Using savepoints in PostgreSQL:

```sql
BEGIN;

INSERT INTO characters (profile_id, name, level)
VALUES (10, 'NewHero', 1);

SAVEPOINT before_optional_gang;

INSERT INTO gang_affiliations (character_id, gang_id, role, join_date)
VALUES (currval('characters_id_seq'), 999, 'MEMBER', CURRENT_DATE);

-- If this fails, roll back only the optional part
ROLLBACK TO SAVEPOINT before_optional_gang;

COMMIT;
```

Pseudo-code in backend:

```text
transaction.begin()

try:
    characterRepository.save(character)
    houseRepository.assignHouse(character.id, house.id)
    auditRepository.log("CHARACTER_CREATED", character.id)
    transaction.commit()
catch error:
    transaction.rollback()
    throw error
```

MongoDB single-document style:

```javascript
db.profiles.updateOne(
  { id: 10, "characters.id": 42 },
  { $set: { "characters.$.balance": 500 } }
)
```

MongoDB multi-document transaction pseudo-code:

```text
session.startTransaction()
try:
    profiles.updateOne(...)
    gangs.updateOne(...)
    session.commitTransaction()
catch error:
    session.abortTransaction()
```

Neo4j transaction example:

```cypher
MATCH (c:Character {id: 42})
MATCH (g:Gang {id: 3})
MERGE (c)-[:MEMBER_OF {role: 'MEMBER'}]->(g);
```

This should run in a transaction so the relationship is only created if both nodes are found and the operation succeeds.

### Discuss

Transactions are one of the strongest tools for correctness, but they should be scoped carefully. The transaction should include all database work that must succeed or fail together, but it should not include slow external work such as HTTP calls, user interaction, file uploads or long computations.

In Bajls, PostgreSQL is the best place for strict transactional source data because it has foreign keys, constraints and mature isolation. MongoDB can be used transactionally, but the better document design is often to keep related data inside one aggregate when possible. Neo4j transactions are useful when several relationships must be updated consistently.

For the migration flow in the project, PostgreSQL is read as the source snapshot, and MongoDB/Neo4j are regenerated as projections. That avoids needing one large distributed transaction across all three databases. Instead, the system accepts that the target databases are rebuilt from the source and should be treated as derived models.

The key design question is how much consistency the use case needs. For critical gameplay state such as ownership, balances and mandatory relations, ACID transactions are appropriate. For cached views, analytics and projections, BASE or eventual consistency can be acceptable.

At exam level, the important point is that transactions are not just `BEGIN` and `COMMIT`. They define correctness boundaries, concurrency behavior, resource usage and failure handling. A good transaction is short, focused, measured, and aligned with the business operation it protects.

## 5. Database Security

### Explain

Database security handler om at beskytte data mod uautoriseret adgang, ændringer, sletning og læk. Det dækker både hvem der må logge ind, hvad de må gøre, hvordan applikationen forbinder til databasen, hvordan queries skrives sikkert, hvordan data krypteres, og hvordan drift som backups og auditing håndteres.

I Bajls-projektet er database security relevant, fordi systemet indeholder profiles, passwords, roles, characters, ownership, gangs og gameplay state. En almindelig spiller skal ikke kunne læse eller ændre andre spilleres private data, og applikationen skal ikke forbinde med en databasebruger, der har flere rettigheder end nødvendigt.

Authentication betyder at bevise, hvem man er. Det kan være database login med username/password, certificates, cloud IAM eller integrated authentication.

Authorization betyder, hvad den autentificerede bruger må gøre. Det styres ofte med roles og privileges:

- **Roles**: grupper eller identiteter, for eksempel `app_user`, `readonly_user`, `admin_user`.
- **Privileges**: konkrete rettigheder som `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `EXECUTE`, `CREATE`.
- **Access granularity**: hvor præcist rettigheder gives, for eksempel database-level, schema-level, table-level, column-level eller row-level.

Least Privilege Principle betyder, at en bruger eller applikation kun skal have de rettigheder, der er nødvendige for dens opgave. En backend application user bør normalt ikke have superuser/admin-rettigheder, ikke kunne droppe tabeller, og ikke kunne oprette nye databasebrugere.

SQL injection er en query security vulnerability, hvor brugerinput bliver sat direkte ind i SQL, så angriberen kan ændre queryens betydning. Det forebygges med parameterized queries, prepared statements, ORM-safe APIs, input validation og ved ikke at bygge SQL med string concatenation.

Data security handler også om:

- **Data at rest**: data gemt på disk, backups eller snapshots. Det kan beskyttes med hashing og encryption.
- **Data in transit**: data på netværket mellem applikation og database. Det bør beskyttes med TLS/SSL.

Hashing og encryption er ikke det samme. Hashing er one-way og bruges typisk til passwords. Encryption er reversible med en key og bruges til data, der skal kunne læses igen, for eksempel følsomme personoplysninger.

Network security betyder at begrænse, hvem der overhovedet kan nå databasen på netværksniveau. En database bør ikke være offentligt eksponeret, fordi den så kan angribes direkte med brute force, exploit scanning, credential stuffing eller denial-of-service.

Operational security handler om drift: backups, restore-procedurer, adgang til backupfiler, kryptering af backups, rotation af secrets og test af restoration.

Auditing betyder at registrere vigtige databasehændelser. Det er vigtigt, fordi man skal kunne se, hvem der gjorde hvad, hvornår og fra hvor, især ved fejl, misbrug eller datalæk.

### Compare

MySQL, PostgreSQL og SQL Server har alle roles/privileges, men de arbejder lidt forskelligt:

- **PostgreSQL** bruger roles til både users og groups. Privileges kan gives på database, schema, table, sequence, function og column level. PostgreSQL understøtter også row-level security.
- **MySQL** bruger accounts som typisk er kombinationer af username og host, for eksempel `'app'@'10.%'`. Privileges kan gives globalt, database-level, table-level og column-level.
- **SQL Server** skelner mellem server-level logins og database-level users. Den har fixed server roles, database roles, schemas, permissions og kan integreres med Windows/Active Directory.

Granularity comparison:

| System | Common access levels | Notes |
| --- | --- | --- |
| PostgreSQL | database, schema, table, column, row, function | strong role model and row-level security |
| MySQL | global, database, table, column, routine | host-based user accounts are common |
| SQL Server | server, database, schema, object, column | strong enterprise integration and fixed roles |

Development vs production:

- Development kan bruge mere fleksible rettigheder, lokal adgang og testdata.
- Production skal bruge stærke passwords/secrets, least privilege, TLS, private networking, backups, auditing og ingen direkte public database exposure.

Application user vs admin user:

- Application user bør kun have nødvendige CRUD-rettigheder.
- Migration/deployment user kan have schema privileges, men bør ikke bruges af runtime-applikationen.
- DBA/admin user bør kun bruges til administration, ikke af applikationskoden.

Hashing vs encryption:

- Hashing bruges til passwords, fordi systemet ikke skal kunne læse det originale password.
- Encryption bruges til data, der skal kunne dekrypteres, for eksempel følsomme fields eller backupfiler.

Auditing at application level vs database level:

- Application-level audit kan registrere business events som `CHARACTER_CREATED` eller `ROLE_CHANGED`.
- Database-level audit kan registrere direkte SQL operations, logins, privilege changes og ændringer på sensitive tabeller.
- Den stærkeste løsning kombinerer begge, fordi de svarer på forskellige spørgsmål.

### Demonstrate

Example roles in PostgreSQL:

```sql
CREATE ROLE bajls_app LOGIN PASSWORD 'change_me';
CREATE ROLE bajls_readonly LOGIN PASSWORD 'change_me';

GRANT CONNECT ON DATABASE bajls_db TO bajls_app;
GRANT USAGE ON SCHEMA public TO bajls_app;

GRANT SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public
TO bajls_app;

GRANT SELECT
ON ALL TABLES IN SCHEMA public
TO bajls_readonly;
```

Runtime application users should normally not get dangerous privileges:

```sql
-- Avoid this for application runtime users
GRANT ALL PRIVILEGES ON DATABASE bajls_db TO bajls_app;
```

Better pattern: separate runtime and migration privileges:

```sql
CREATE ROLE bajls_runtime LOGIN PASSWORD 'change_me';
CREATE ROLE bajls_migration LOGIN PASSWORD 'change_me';

GRANT SELECT, INSERT, UPDATE, DELETE
ON ALL TABLES IN SCHEMA public
TO bajls_runtime;

GRANT CREATE, USAGE
ON SCHEMA public
TO bajls_migration;
```

Column-level restriction example:

```sql
REVOKE SELECT (password)
ON profiles
FROM bajls_readonly;
```

View to avoid exposing sensitive fields:

```sql
CREATE VIEW public_profile_view AS
SELECT id, username, role
FROM profiles;

GRANT SELECT ON public_profile_view TO bajls_readonly;
```

Unsafe SQL injection pattern:

```text
sql = "SELECT * FROM profiles WHERE username = '" + inputUsername + "'"
```

If `inputUsername` is:

```text
' OR '1' = '1
```

the query can be changed to return more rows than intended.

Safe parameterized query:

```java
String sql = "SELECT * FROM profiles WHERE username = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, username);
```

JPA-style safe query:

```java
Profile profile = entityManager
    .createQuery("SELECT p FROM Profile p WHERE p.username = :username", Profile.class)
    .setParameter("username", username)
    .getSingleResult();
```

Password hashing pseudo-code:

```text
passwordHash = bcrypt.hash(rawPassword)
store passwordHash

login:
    storedHash = findHashByUsername(username)
    if bcrypt.verify(rawPassword, storedHash):
        allow login
```

TLS database connection example as configuration idea:

```text
jdbc:postgresql://db.internal:5432/bajls_db?sslmode=require
```

Network security examples:

```text
Allow:
    app-server-private-ip -> database:5432

Deny:
    public internet -> database:5432
```

Audit table example:

```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(20) NOT NULL,
    row_id BIGINT,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Audit trigger example:

```sql
CREATE OR REPLACE FUNCTION audit_character_changes()
RETURNS trigger AS $$
BEGIN
    INSERT INTO audit_log (table_name, operation, row_id, changed_by)
    VALUES ('characters', TG_OP, COALESCE(NEW.id, OLD.id), current_user);

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_characters
AFTER INSERT OR UPDATE OR DELETE ON characters
FOR EACH ROW
EXECUTE FUNCTION audit_character_changes();
```

### Discuss

Database security should be layered. No single technique is enough. Strong passwords do not help if the database is public and the app uses an admin account. TLS does not help if SQL injection allows attackers to read all data. Backups are not safe if backup files are unencrypted and accessible to everyone.

For Bajls, a good production design would use separate users for runtime, migrations and read-only reporting. The runtime user should only have CRUD access to the tables it actually needs. Admin tasks should use separate credentials. Passwords should be hashed, sensitive fields should not be exposed through DTOs or views, and database connections should use private networking and TLS.

The database should not be publicly exposed because the database is not the public API. The backend should be the controlled entry point where authentication, authorization, validation, rate limiting and logging happen. If the database is open to the internet, attackers can bypass application logic and attack the storage layer directly.

Backups must be protected like production data. They should be encrypted, access-controlled, stored separately from the main database, and restoration should be tested. A backup that cannot be restored is not useful, and a backup that leaks is still a data breach.

Auditing is important because prevention is never perfect. The system should audit logins, failed login attempts, privilege changes, schema changes, access to sensitive data, updates/deletes on important tables, and business-critical events such as role changes or ownership changes. Audit logs should be append-only where possible and protected from modification by normal application users.

The open reasoning point is that security always balances usability, maintainability and risk. Too few controls create obvious vulnerabilities. Too many manual controls can make developers bypass them. A good design uses least privilege, secure defaults, automation, monitoring and clear separation between development and production.

## 6. Backup, Recovery & Reliability

### Explain

Backup and recovery handler om at kunne gendanne data efter fejl, sletning, corruption, hardware failure, ransomware, deployment-fejl eller menneskelige fejl. Reliability handler om, hvor sikkert systemet kan blive ved med at fungere og gendannes, når noget går galt.

Der findes flere backup-typer:

- **Full backup**: en komplet kopi af hele databasen.
- **Incremental backup**: gemmer kun ændringer siden sidste backup, uanset om sidste backup var full eller incremental.
- **Differential backup**: gemmer ændringer siden sidste full backup.

Point-in-time recovery betyder, at man kan gendanne databasen til et bestemt tidspunkt, for eksempel lige før en forkert `DELETE` blev kørt. I PostgreSQL gøres dette typisk med en base backup plus WAL logs. WAL står for Write-Ahead Log og indeholder ændringer, som kan replayes frem til et bestemt tidspunkt.

Backup consistency handler om, om backupen repræsenterer en korrekt database state:

- **Cold backup**: databasen stoppes først, og filerne kopieres derefter. Det er simpelt og konsistent, men kræver downtime.
- **Hot backup**: databasen er online, mens backupen tages. Det kræver database-support for at sikre konsistens, men giver mindre eller ingen downtime.

Restore testing betyder at teste, at backups faktisk kan gendannes. En backup er kun værdifuld, hvis man ved, at den kan restore korrekt, hurtigt og med den forventede data.

I Bajls-projektet er PostgreSQL den vigtigste database at beskytte, fordi den fungerer som source of truth. MongoDB og Neo4j bliver genereret som projektioner fra PostgreSQL i migrationsflowet. Derfor er PostgreSQL-backup vigtigst, mens MongoDB/Neo4j enten kan backupes separat eller rebuildes fra PostgreSQL, afhængigt af production-design.

### Compare

Full backup:

- Fordel: nemmest at forstå og restore.
- Ulempe: tager mest plads og tid.
- God til: baseline backups og mindre databaser.

Incremental backup:

- Fordel: hurtig og pladsbesparende.
- Ulempe: restore kan kræve mange backup pieces i korrekt rækkefølge.
- God til: hyppige backups med lav storage overhead.

Differential backup:

- Fordel: restore kræver kun full backup + seneste differential.
- Ulempe: differential backups vokser, jo længere tid der går siden sidste full backup.
- God til: balance mellem simpel restore og mindre backup-størrelse end full.

Hot vs cold backup:

- Cold backup er simpel og meget konsistent, men kræver downtime.
- Hot backup passer bedre til production, men kræver korrekt databaseværktøj og log management.

Point-in-time recovery vs normal restore:

- Normal restore gendanner til backupens tidspunkt.
- Point-in-time recovery kan gendanne til et præcist tidspunkt mellem backups.

For Bajls ville en simpel dev-strategi være at gemme `sqls/seed.sql` og kunne reseede databasen. En production-strategi skal være stærkere: automatiske full backups, WAL archiving, restore tests, adgangskontrol og kryptering af backupfiler.

### Demonstrate

PostgreSQL full logical backup:

```bash
pg_dump -h localhost -U postgres -d bajls_db -F c -f bajls_full.backup
```

Restore from logical backup:

```bash
pg_restore -h localhost -U postgres -d bajls_db --clean --if-exists bajls_full.backup
```

Plain SQL backup:

```bash
pg_dump -h localhost -U postgres -d bajls_db > bajls_backup.sql
```

Restore plain SQL backup:

```bash
psql -h localhost -U postgres -d bajls_db < bajls_backup.sql
```

Conceptual point-in-time recovery flow:

```text
1. Take a base backup at 02:00.
2. Archive WAL logs continuously.
3. User accidentally deletes data at 10:15.
4. Restore base backup.
5. Replay WAL logs until 10:14:59.
6. Start database from that recovered state.
```

Example PostgreSQL recovery target idea:

```text
restore_command = 'copy C:\\wal_archive\\%f %p'
recovery_target_time = '2026-06-08 10:14:59'
```

Backup consistency example:

```text
Cold backup:
    stop database
    copy database files
    start database

Hot backup:
    database stays online
    backup tool copies consistent snapshot
    WAL logs are archived for recovery
```

Restore testing checklist:

```text
1. Restore backup into a separate test environment.
2. Run schema checks.
3. Count important rows.
4. Test login/profile/character API flows.
5. Verify constraints and indexes.
6. Measure restore time.
7. Document recovery steps.
```

Example validation queries after restore:

```sql
SELECT COUNT(*) FROM profiles;
SELECT COUNT(*) FROM characters;
SELECT COUNT(*) FROM gang_affiliations;

SELECT c.id, c.name, p.username
FROM characters c
JOIN profiles p ON p.id = c.profile_id
LIMIT 10;
```

### Discuss

A backup strategy should be designed around two business goals:

- **RPO, Recovery Point Objective**: how much data can we afford to lose?
- **RTO, Recovery Time Objective**: how long can the system be down while restoring?

If Bajls can afford to lose one day of data, daily full backups may be enough. If it can only afford to lose a few minutes, it needs frequent backups or WAL-based point-in-time recovery. If downtime must be low, restore procedures must be automated and tested.

Reliability is not just having backups. It also requires monitoring, alerts, replication, access control, encryption, documentation and regular restore drills. Many systems have backups but fail during restore because nobody tested credentials, commands, backup integrity or restore time.

For this project, PostgreSQL should be treated as the primary recovery target because MongoDB and Neo4j are derived from it. After restoring PostgreSQL, the migration can rebuild MongoDB and Neo4j. In a real production system, if MongoDB and Neo4j accept independent writes, then they need their own backup and recovery strategies too.

Backups must also be secured. Backup files can contain the same sensitive data as production, sometimes in an easier-to-steal format. They should be encrypted, access-controlled, stored separately, and protected from accidental deletion.

The open reasoning point is that backup design is about risk. More frequent backups, PITR and replication improve recovery, but cost more in storage, complexity and operations. The right solution depends on how valuable the data is, how quickly the system must recover, and how much data loss is acceptable.

## 7. Join Tables

### Explain

A join table is used in a relational database to model a many-to-many relationship. If one character can join many gangs, and one gang can have many characters, the relationship cannot be stored as a single foreign key on only one side. Instead, we create a join table that stores references to both tables.

In Bajls, examples of join-table-like relations are:

- `gang_affiliations`: connects characters and gangs.
- `character_quest`: connects characters and quests.
- `character_drug`: connects characters and drugs.

There are two common designs:

1. Composite primary key using the foreign keys:

```sql
CREATE TABLE character_quest (
    character_id BIGINT NOT NULL REFERENCES characters(id),
    quest_id BIGINT NOT NULL REFERENCES quests(id),
    status VARCHAR(30),
    accepted_at TIMESTAMP,
    PRIMARY KEY (character_id, quest_id)
);
```

2. Separate surrogate primary key plus foreign keys:

```sql
CREATE TABLE character_quest (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL REFERENCES characters(id),
    quest_id BIGINT NOT NULL REFERENCES quests(id),
    status VARCHAR(30),
    accepted_at TIMESTAMP,
    UNIQUE (character_id, quest_id)
);
```

A composite primary key is simple and expresses that the pair of IDs is the identity of the relationship. A separate `id` is useful when the relationship needs to be referenced elsewhere, updated independently, audited, or represented as an entity in backend code.

A join table becomes a real entity when the relationship has its own attributes or behavior. For example, `gang_affiliations` is not just "character belongs to gang". It can contain `role`, `join_date` or other membership-specific data. That means it represents a domain concept: a gang membership.

Cascading rules define what happens to join table records when parent rows are deleted. If a character is deleted, related records in `gang_affiliations`, `character_quest` and `character_drug` should usually be deleted too, because they cannot exist without the character. This can be handled with `ON DELETE CASCADE`.

### Compare

Composite PK advantages:

- no extra ID column
- naturally prevents duplicate pairs
- good for simple many-to-many relations
- clear relational meaning

Composite PK disadvantages:

- more awkward in backend models, especially with JPA/Hibernate
- harder to reference from other tables
- can make APIs less convenient because the identity is two fields
- updates to key values are more complex

Surrogate PK advantages:

- simple backend identity field
- easier entity modeling
- easier auditing and referencing
- works well when the join table has extra attributes
- often easier for REST endpoints, DTOs and repositories

Surrogate PK disadvantages:

- requires an extra `UNIQUE` constraint to prevent duplicate relationships
- slightly more storage
- can hide the natural uniqueness of the relationship if constraints are missing

Simple join table vs real entity:

- If the table only contains two foreign keys, it is usually just a join table.
- If it contains attributes like `join_date`, `status`, `role`, `accepted_at`, `quantity` or audit fields, it is usually a real entity.
- If the application needs CRUD operations on the relationship itself, it should usually be modeled as an entity in the backend.

Cascading choices:

- `ON DELETE CASCADE`: delete join records automatically when parent is deleted.
- `ON DELETE RESTRICT` / `NO ACTION`: prevent deleting parent rows while relationships exist.
- `ON DELETE SET NULL`: rarely useful for mandatory join tables, because foreign keys are normally required.

Document and graph models handle join tables differently:

- In MongoDB, a join table can become an embedded array inside an aggregate document, or references between collections.
- In Neo4j, a join table usually becomes a relationship between two nodes. If the join table has attributes, those attributes become relationship properties.

### Demonstrate

Simple many-to-many table with composite primary key:

```sql
CREATE TABLE character_drug (
    character_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    PRIMARY KEY (character_id, drug_id),
    CONSTRAINT fk_character_drug_character
        FOREIGN KEY (character_id) REFERENCES characters(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_character_drug_drug
        FOREIGN KEY (drug_id) REFERENCES drugs(id)
        ON DELETE CASCADE
);
```

Join table as real entity with separate primary key:

```sql
CREATE TABLE gang_affiliations (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL,
    gang_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    join_date DATE NOT NULL,
    CONSTRAINT uq_gang_affiliation UNIQUE (character_id, gang_id),
    CONSTRAINT fk_gang_affiliation_character
        FOREIGN KEY (character_id) REFERENCES characters(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_gang_affiliation_gang
        FOREIGN KEY (gang_id) REFERENCES gangs(id)
        ON DELETE CASCADE
);
```

Query using a join table:

```sql
SELECT c.name AS character_name,
       g.name AS gang_name,
       ga.role,
       ga.join_date
FROM gang_affiliations ga
JOIN characters c ON c.id = ga.character_id
JOIN gangs g ON g.id = ga.gang_id
WHERE c.id = 42;
```

Backend pseudo-model when the join table is a real entity:

```java
class GangAffiliation {
    Long id;
    GameCharacter character;
    Gang gang;
    String role;
    LocalDate joinDate;
}
```

MongoDB embedded representation:

```javascript
{
  id: 10,
  username: "player1",
  characters: [
    {
      id: 42,
      name: "ShadowKnight",
      gangs: [
        {
          gangId: 3,
          gangName: "North Syndicate",
          role: "MEMBER",
          joinDate: "2026-05-01"
        }
      ]
    }
  ]
}
```

Neo4j representation:

```cypher
MATCH (c:Character {id: 42})
MATCH (g:Gang {id: 3})
MERGE (c)-[:MEMBER_OF {
  role: 'MEMBER',
  joinDate: '2026-05-01'
}]->(g);
```

Query in Neo4j:

```cypher
MATCH (c:Character {id: 42})-[m:MEMBER_OF]->(g:Gang)
RETURN c.name, g.name, m.role, m.joinDate;
```

### Discuss

The choice between composite primary key and surrogate primary key depends on how important the relationship is as a domain concept. If it is only a technical link, a composite key is clean and compact. If the relationship has attributes, lifecycle, audit requirements or API endpoints, a separate primary key often makes the backend simpler.

In Bajls, `gang_affiliations` should be treated as a real entity because membership has meaning beyond the two IDs. A character's gang role and join date belong to the relationship itself, not only to the character or only to the gang. The same reasoning can apply to `character_quest`, where `status` and `accepted_at` describe the character's personal progress on a quest.

Cascading must match business rules. Deleting a character should probably delete that character's memberships and quest progress. But deleting a gang may be more sensitive: should all membership history disappear, or should the system prevent deleting a gang that still has members? The correct answer depends on whether historical data matters.

When moving to MongoDB, join tables often become embedded relationship data inside the aggregate that is most commonly read. In Bajls, profile-centered reads make it natural to embed a character's memberships and quest progress inside the profile document. The trade-off is that updates can become more complex if the same relationship data is duplicated.

When moving to Neo4j, join tables map very naturally to relationships. A relational join table is often just an edge in a graph. If the join table has columns, those columns become relationship properties. This is one reason Neo4j fits relationship-heavy parts of the Bajls domain well.

The open reasoning point is that many-to-many relationships are not just technical schema details. They often reveal important domain concepts. When the relationship starts carrying meaning, time, status or behavior, it should be modeled explicitly instead of treated as a hidden join table.

## 8. Query Example Across PostgreSQL, MongoDB and Neo4j

### Explain

Et godt query-scenarie i Bajls-projektet er:

**Find en character, dens profile, house, garage, vehicles, gang memberships og quests.**

I et ERD/relational model starter man med entities og relations:

```text
profiles 1----* characters
characters 1----0..1 houses
characters 1----0..1 garages
garages 1----* vehicles
characters *----* gangs through gang_affiliations
characters *----* quests through character_quest
```

I PostgreSQL betyder det, at data er normaliseret i flere tabeller. Queryen skal samle data med joins. Det giver stærk integritet, fordi foreign keys og constraints sikrer, at relationerne er gyldige.

I MongoDB kan samme data modelleres som et profile-centered document. En profile kan indeholde embedded characters, og hver character kan indeholde house, garage, vehicles, gangs og quests som nested data. Queryen bliver derfor ofte et enkelt document lookup.

I Neo4j modelleres samme domæne som nodes og relationships. `Profile`, `Character`, `House`, `Garage`, `Vehicle`, `Gang` og `Quest` bliver nodes, mens ownership, membership og quest progress bliver relationships.

### Compare

PostgreSQL:

- Queryen er mere kompleks, fordi den kræver joins.
- Den er stærk til consistency, constraints og præcise relationer.
- Den skalerer godt for OLTP, hvis indexes og query plans er gode.
- Den kan blive tung, hvis man ofte skal hente store object graphs.

MongoDB:

- Queryen kan være meget simpel og hurtig, hvis data er embedded rigtigt.
- Den er stærk til read-heavy aggregate views.
- Den kan blive sværere at holde konsistent, hvis samme data duplikeres flere steder.
- Store documents kan blive tunge at opdatere.

Neo4j:

- Queryen er naturlig, hvis fokus er relationer og traversal.
- Den er stærk, når spørgsmålet handler om connected data.
- Den kan være hurtigere end relationelle joins for dybe relationer.
- Den er ikke altid bedst til simple tabulære rapporter eller store aggregations.

Generel kompleksitet:

| Database | Query complexity | Speed/scalability strength |
| --- | --- | --- |
| PostgreSQL | medium/high with many joins | strong consistency and indexed joins |
| MongoDB | low if aggregate is embedded | fast profile/document reads |
| Neo4j | low/medium for relationships | fast traversal and relationship queries |

### Demonstrate

PostgreSQL query:

```sql
SELECT p.username,
       c.id AS character_id,
       c.name AS character_name,
       h.address AS house_address,
       g.id AS garage_id,
       v.name AS vehicle_name,
       gang.name AS gang_name,
       ga.role AS gang_role,
       q.name AS quest_name,
       cq.status AS quest_status
FROM characters c
JOIN profiles p ON p.id = c.profile_id
LEFT JOIN houses h ON h.character_id = c.id
LEFT JOIN garages g ON g.character_id = c.id
LEFT JOIN vehicles v ON v.garage_id = g.id
LEFT JOIN gang_affiliations ga ON ga.character_id = c.id
LEFT JOIN gangs gang ON gang.id = ga.gang_id
LEFT JOIN character_quest cq ON cq.character_id = c.id
LEFT JOIN quests q ON q.id = cq.quest_id
WHERE c.id = 42;
```

MongoDB query:

```javascript
db.profiles.findOne(
  { "characters.id": 42 },
  {
    username: 1,
    characters: {
      $elemMatch: { id: 42 }
    }
  }
)
```

Possible MongoDB document shape:

```javascript
{
  id: 10,
  username: "player1",
  characters: [
    {
      id: 42,
      name: "ShadowKnight",
      house: { id: 5, address: "North Road" },
      garage: {
        id: 8,
        vehicles: [
          { id: 2, name: "Street Racer" }
        ]
      },
      gangs: [
        { id: 3, name: "North Syndicate", role: "MEMBER" }
      ],
      quests: [
        { id: 7, name: "First Mission", status: "ACTIVE" }
      ]
    }
  ]
}
```

Neo4j query:

```cypher
MATCH (p:Profile)-[:OWNS]->(c:Character {id: 42})
OPTIONAL MATCH (c)-[:OWNS_HOUSE]->(h:House)
OPTIONAL MATCH (c)-[:OWNS_GARAGE]->(g:Garage)
OPTIONAL MATCH (g)-[:CONTAINS]->(v:Vehicle)
OPTIONAL MATCH (c)-[m:MEMBER_OF]->(gang:Gang)
OPTIONAL MATCH (c)-[cq:HAS_QUEST]->(q:Quest)
RETURN p.username,
       c.name,
       h.address,
       g.id,
       collect(DISTINCT v.name) AS vehicles,
       collect(DISTINCT {gang: gang.name, role: m.role}) AS gangs,
       collect(DISTINCT {quest: q.name, status: cq.status}) AS quests;
```

### Discuss

Det samme spørgsmål får forskellig form afhængigt af databasen. I PostgreSQL starter man fra ERD og normaliserede tabeller. I MongoDB starter man fra det aggregate, applikationen ofte skal læse. I Neo4j starter man fra relationerne og de paths, man vil traverse.

I Bajls giver PostgreSQL mest mening som source of truth, fordi domain rules og foreign keys er vigtige. MongoDB giver mening til hurtige profile views. Neo4j giver mening til social/relationship gameplay, for eksempel gang networks og quest relations.

Den åbne vurdering er, at der ikke findes én "hurtigste" database generelt. Den hurtigste løsning afhænger af query pattern, datamodel, indexes, document structure, graph structure og hvor ofte data læses vs ændres.

## 9. Database Technologies

### Explain

Database technologies kan opdeles efter datamodel og workload. DB-Engines opdeler blandt andet databasesystemer i kategorier som relational DBMS, key-value stores, document stores, time series DBMS, graph DBMS, search engines, vector DBMS, object-oriented DBMS, RDF stores, wide-column stores, spatial DBMS, native XML DBMS, event stores, columnar DBMS og multi-model DBMS.

De vigtigste typer i dette kursus/projekt er:

- **Relational databases**: data i tabeller med rows, columns, primary keys, foreign keys og SQL.
- **Document databases**: data som documents, typisk JSON/BSON.
- **Graph databases**: data som nodes og relationships.

Andre vigtige teknologier:

- **Key-value stores**: simple opslag fra key til value.
- **Wide-column stores**: store distribuerede tabeller med fleksible columns.
- **Columnar databases**: data gemmes kolonneorienteret, ofte til analytics.
- **Time-series databases**: optimeret til tidsstemplede målinger.
- **Search engines**: optimeret til text search og relevance ranking.
- **Vector databases**: optimeret til similarity search på embeddings.
- **RDF/triple stores**: graph-lignende semantic web data med triples.
- **Event stores**: gemmer en historik af events som source of truth.
- **Spatial databases**: optimeret til geografiske/geometriske queries.

Multi-model databases understøtter flere datamodeller i samme databasesystem. Eksempelvis kan én database understøtte document, graph, key-value og search-lignende features. Ideen er at give fleksibilitet uden at skulle drive mange separate databaseprodukter.

### Compare

Relational DBs:

- Stærke til structured data, constraints, joins og ACID transactions.
- Gode til financial systems, ERP, booking, inventory, users og ownership.
- Eksempler: PostgreSQL, MySQL, SQL Server, Oracle.

Document DBs:

- Stærke til fleksible schemas og aggregate reads.
- Gode til content, profiles, catalogs, product data og API-centric apps.
- Eksempler: MongoDB, Couchbase.

Graph DBs:

- Stærke til relationships, traversals og path queries.
- Gode til social networks, recommendations, fraud detection, network analysis og access graphs.
- Eksempler: Neo4j, Amazon Neptune.

Key-value stores:

- Meget hurtige simple lookups.
- Gode til caching, sessions, feature flags og counters.
- Eksempler: Redis, DynamoDB i key-value style.

Columnar databases:

- Stærke til OLAP og analytics over mange rows men få columns.
- Gode til dashboards, reporting og data warehouses.
- Eksempler: ClickHouse, BigQuery, Snowflake.

Search engines:

- Stærke til full-text search, relevance og filtering.
- Gode til search bars, logs og document discovery.
- Eksempler: Elasticsearch, OpenSearch, Solr.

Vector databases:

- Stærke til nearest-neighbor search på embeddings.
- Gode til semantic search, recommendations og AI retrieval.
- Eksempler: Pinecone, Weaviate, Milvus, pgvector.

Multi-model databases:

- Fordel: færre systemer at drive og mere fleksibilitet.
- Ulempe: kan være mindre specialiseret end et dedikeret system.
- Eksempler: ArangoDB, Azure Cosmos DB, Oracle, PostgreSQL med JSONB/pgvector/extensions.

### Demonstrate

Relational example:

```sql
SELECT c.name, p.username
FROM characters c
JOIN profiles p ON p.id = c.profile_id
WHERE p.username = 'player1';
```

Document example:

```javascript
db.profiles.findOne({ username: "player1" })
```

Graph example:

```cypher
MATCH (p:Profile {username: 'player1'})-[:OWNS]->(c:Character)
RETURN c.name;
```

Key-value example:

```text
SET session:abc123 "{ userId: 10, role: 'USER' }"
GET session:abc123
```

Time-series example:

```sql
SELECT time_bucket('1 hour', created_at), COUNT(*)
FROM login_events
GROUP BY 1;
```

Search engine example:

```text
Find quests where text matches "dragon cave" and rank best matches first.
```

Vector search example:

```text
Find the 10 quests whose embeddings are closest to the player's current objective.
```

### Discuss

Valget af database bør følge access patterns og consistency requirements. Hvis data er stærkt struktureret og forretningsreglerne er vigtige, er en relational database ofte bedst. Hvis API'et næsten altid læser et samlet aggregate, kan document database være bedre. Hvis relationer og paths er selve spørgsmålet, er graph database stærk.

Bajls-projektet viser netop polyglot persistence: samme domain data kan repræsenteres i PostgreSQL, MongoDB og Neo4j. Det giver læring og fleksibilitet, men også kompleksitet, fordi modellerne skal holdes i sync.

Multi-model databases kan reducere kompleksitet, men de fjerner ikke behovet for god datamodellering. Selv i en multi-model database skal man stadig beslutte, om en relation bør være en foreign key, embedded document, edge, index eller projection.

Kilde: DB-Engines ranking categories, juni 2026: https://db-engines.com/en/ranking_categories

## 10. Distributed Databases

### Explain

Distributed databases fordeler data eller kopier af data over flere servere. Formålet kan være bedre performance, højere availability, mere storage capacity eller geografisk nærhed til brugere.

Sharding betyder, at data deles horisontalt på tværs af flere servere. Hver shard indeholder en del af dataen. For eksempel kan profiles med `id 1-1000000` ligge på shard A, og profiles med `id 1000001-2000000` på shard B.

Sharding løser især:

- for meget data til én server
- for høj write/read load til én server
- behov for horisontal scaling

Prisen ved sharding:

- mere kompleks routing
- sværere joins på tværs af shards
- sværere transactions
- risiko for hot shards
- vanskeligere backup, migration og rebalancing

Replication betyder, at den samme data kopieres til flere servere. En primary kan modtage writes, mens replicas kan bruges til reads eller failover.

Replication løser især:

- højere availability
- read scaling
- disaster recovery
- lavere latency ved geografiske replicas

Prisen ved replication:

- replication lag
- stale reads
- failover-kompleksitet
- konfliktløsning ved multi-primary setups
- mere storage og drift

Partitioning betyder, at en stor tabel deles i mindre dele. Det kan ske inden for samme database server. Sharding er typisk partitioning på tværs af flere servere. Derfor kan man sige: partitioning handler om at dele data logisk/fysisk; sharding er distributed horizontal partitioning.

Vertical scaling betyder at gøre én server stærkere: mere CPU, RAM, disk og I/O. Horizontal scaling betyder at tilføje flere servere.

CAP theorem siger, at et distributed system ved network partition ikke kan garantere både stærk consistency og availability samtidig. Man må vælge, om systemet hellere vil afvise/vente på requests for at bevare consistency, eller svare videre med risiko for midlertidig inkonsistens.

### Compare

Vertical scaling:

- nemmere at administrere
- kræver færre arkitekturændringer
- har en fysisk og økonomisk grænse
- single-server bottleneck kan stadig eksistere

Horizontal scaling:

- kan vokse ved at tilføje flere noder
- bedre fault tolerance
- mere kompleks data distribution
- kræver ofte sharding, replication eller distributed coordination

Sharding vs partitioning:

- Partitioning kan være lokalt i én database.
- Sharding spreder partitions over flere servere.
- Partitioning hjælper query performance og management.
- Sharding hjælper også med kapacitet og distributed load.

Replication vs sharding:

- Replication kopierer samme data.
- Sharding splitter forskellig data.
- Replication hjælper availability og read scaling.
- Sharding hjælper storage og write/load scaling.

Consistency vs availability:

- Stærk consistency betyder, at alle brugere ser nyeste korrekte data.
- Høj availability betyder, at systemet fortsætter med at svare, selv ved fejl.
- I distributed systems kan man ofte ikke få begge perfekt under network partitions.

ACID vs eventual consistency:

- ACID passer til balances, ownership, inventory og vigtige constraints.
- Eventual consistency passer til feeds, counters, analytics, caches og projections.

### Demonstrate

Sharding example:

```text
Shard key: profile_id

Shard A: profile_id 1 - 999999
Shard B: profile_id 1000000 - 1999999
Shard C: profile_id 2000000 - 2999999
```

Query routing:

```text
getProfile(1500000)
-> router checks shard key
-> query goes to Shard B
```

Hash-based sharding:

```text
shard = hash(profile_id) % number_of_shards
```

PostgreSQL partitioning example:

```sql
CREATE TABLE audit_log (
    id BIGSERIAL,
    changed_at TIMESTAMP NOT NULL,
    table_name VARCHAR(100),
    operation VARCHAR(20)
) PARTITION BY RANGE (changed_at);

CREATE TABLE audit_log_2026_06
PARTITION OF audit_log
FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
```

Replication example:

```text
Primary database:
    handles writes

Read replica 1:
    handles read queries

Read replica 2:
    used for reporting or failover
```

Eventual consistency example:

```text
1. User completes a quest in PostgreSQL.
2. PostgreSQL commits the transaction immediately.
3. A background migration/projection updates MongoDB and Neo4j.
4. For a short time, MongoDB/Neo4j may show the old quest status.
5. After the update finishes, all databases show the same state.
```

This is acceptable for read projections, but not for critical balance transfers.

### Discuss

Distributed databases solve real scaling and reliability problems, but they introduce complexity. A single PostgreSQL server is simpler to reason about than a sharded, replicated, multi-region database. Therefore, distribution should be introduced when there is a real need: too much data, too much traffic, availability requirements or geographic latency.

For Bajls, the current project does not need true sharding, because it is a course project with limited data. But conceptually, if the RPG became very large, sharding by `profile_id` could keep each player's data close together. That would make profile-centered reads efficient, but cross-player analytics and global gang queries could become harder.

Replication would be more realistic as a first scaling step. PostgreSQL could have read replicas for reporting or profile reads, while the primary handles writes. MongoDB and Neo4j projections already act somewhat like specialized read models, although they are not exact replicas.

CAP theorem is important because it explains why distributed systems involve trade-offs. If the network breaks between nodes, the system must choose between rejecting operations to preserve consistency or continuing to answer with possible stale data. For gameplay-critical data, consistency should usually win. For public lists, recommendations or cached views, availability and eventual consistency may be acceptable.

The open reasoning point is that distributed design should be driven by business requirements, not hype. Sharding, replication and partitioning are powerful, but each one adds operational cost. The best design is the simplest one that meets performance, reliability and consistency requirements.

## 11. Data Layer

### Explain

Data layer er den del af backend, der håndterer adgang til databasen. Det ligger mellem controllers/API-routes og selve databasen. I Bajls-projektet består data layer især af:

- **Models/entities** i `persistence.entity`, for eksempel `Profile`, `GameCharacter`, `House`, `Garage`, `Vehicle`, `GangAffiliation`, `CharacterQuest`, `CharacterDrug` og `Transaction`.
- **DTOs** i `app.dto`, for eksempel `ProfileDTO`, `GameCharacterDTO` og `TransactionDTO`.
- **Repositories/DAOs** i `app.dao`, `app.mongo` og `app.neo4j`.
- **Transaction logic** i `TransactionService`.
- **Persistence setup** i `AppPersistence`, `JpaAppPersistence`, `MongoAppPersistence` og `Neo4jAppPersistence`.

For PostgreSQL bruger projektet JPA/Hibernate. `JpaDao<T>` implementerer generic CRUD med `EntityManager`, `persist`, `merge`, `remove`, `find` og Criteria API. For MongoDB og Neo4j bruges native drivers/repositories, hvor projektet selv skriver document operations og Cypher queries.

ORM betyder Object-Relational Mapping. Hibernate mapper Java objects til relationelle tabeller. Det gør CRUD og relationer nemmere at arbejde med, men det kan også skjule performance-problemer. Native drivers betyder, at man skriver mere direkte mod databasen, for eksempel MongoDB filters eller Neo4j Cypher.

DTO Pattern betyder, at API'et ikke returnerer entity objects direkte, men særlige transfer objects. I Bajls bruges DTOs til at forme API-responses og undgå at eksponere interne felter som password. `RouteQueries` bruger JPQL constructor expressions som:

```java
SELECT new app.dto.ProfileDTO(p.id, p.firstName, p.lastName, p.email, p.username, p.role)
FROM Profile p
```

Transaction boundaries i koden betyder, hvor en database transaction starter og slutter. I `JpaDao.save`, `update` og `deleteById` starter transaction med `tx.begin()`, udfører arbejdet, kalder `flush()`, og afslutter med `tx.commit()`. Hvis noget fejler, kaldes `rollbackIfActive(tx)`.

Repository pattern betyder, at data access samles bag metoder som `findAll`, `findById`, `save`, `update` og `deleteById`. Controllers skal derfor ikke kende detaljer om SQL, MongoDB documents eller Cypher. Repositoryets ansvar er at hente og gemme data, ikke at indeholde hele business logic.

### Compare

ORM vs native driver:

| Approach | Pros | Cons |
| --- | --- | --- |
| ORM/JPA | hurtig CRUD, entity mapping, transactions, relationer, mindre boilerplate | kan skjule SQL, N+1, lazy loading fejl, tunge object graphs |
| Native driver | mere kontrol, tydelige queries, database-specifikke features | mere boilerplate, mindre portability, mere manuelt mapping-arbejde |

I Bajls passer ORM godt til PostgreSQL, fordi domain model og relationer er stærke. Native MongoDB og Neo4j repositories passer bedre til de to NoSQL-modeller, fordi deres datamodeller ikke er relationelle.

ORM kan skjule performance-problemer, fordi en simpel Java-linje kan udløse ekstra SQL. For eksempel kan `character.getProfile().getUsername()` udløse en ekstra query, hvis `profile` er lazy-loaded og ikke allerede hentet. Man kan opdage det ved at:

- slå SQL logging til, for eksempel `hibernate.show_sql`
- bruge `EXPLAIN ANALYZE` på SQL queries
- måle antal queries pr. API request
- bruge Hibernate statistics eller profiler
- læse execution plans og slow query logs

DTOs vs entities:

- Entities repræsenterer persistence model og relationer.
- DTOs repræsenterer API contract.
- DTOs beskytter mod overexposure og recursive serialization.
- Entities er bedre internt i transaction/business logic.

Application validation vs database validation:

- **Application-level validation** sker før data sendes til databasen. Det giver gode fejlbeskeder og kan validere business rules.
- **Database-level validation** sker med constraints som `NOT NULL`, `UNIQUE`, foreign keys og check constraints. Det beskytter data uanset hvilken applikation der skriver.

Optimistic vs pessimistic locking:

- **Optimistic locking** antager, at konflikter er sjældne. Man bruger ofte en `version` column. Hvis versionen er ændret ved commit, fejler transactionen.
- **Pessimistic locking** låser data tidligt, for eksempel med `SELECT ... FOR UPDATE`, så andre transactions må vente.

I Bajls bruger `GameCharacter` en `@Version` column, og `TransactionService` bruger `LockModeType.OPTIMISTIC` ved balanceændringer.

### Demonstrate

Simplified data layer flow:

```text
HTTP Controller
    -> Service / Repository
        -> EntityManager / Mongo Driver / Neo4j Driver
            -> Database
        <- Entity
    <- DTO
<- JSON response
```

Generic JPA repository pattern:

```java
public T save(T entity) {
    EntityTransaction tx = null;
    try (EntityManager em = entityManagerFactory.createEntityManager()) {
        tx = em.getTransaction();
        tx.begin();
        em.persist(entity);
        em.flush();
        tx.commit();
        return entity;
    } catch (RuntimeException e) {
        rollbackIfActive(tx);
        throw e;
    }
}
```

Native MongoDB repository style:

```java
collection.replaceOne(
    Filters.eq("id", id),
    toDocument(entity),
    new ReplaceOptions().upsert(true)
);
```

Native Neo4j repository style:

```java
session.executeWrite(tx -> {
    tx.run(
        "MERGE (n:" + label + " {id: $id}) SET n = $props",
        Map.of("id", id, "props", props)
    );
    return null;
});
```

DTO projection query:

```java
SELECT new app.dto.GameCharacterDTO(
    c.id,
    c.name,
    c.balance,
    c.profile.id,
    c.gender,
    c.skincolor,
    c.eyecolor,
    c.height,
    c.weight,
    c.house.id,
    c.garage.id
)
FROM GameCharacter c
```

N+1 problem:

```text
characters = characterRepository.findAll()

for character in characters:
    profileName = character.getProfile().getUsername()
```

This can create 1 query for all characters + 1 query per character profile.

Solutions:

```sql
-- SQL style
SELECT c.id, c.name, p.username
FROM characters c
JOIN profiles p ON p.id = c.profile_id;
```

```java
// JPQL fetch join style
SELECT c
FROM GameCharacter c
JOIN FETCH c.profile
```

```java
// DTO projection style
SELECT new app.dto.GameCharacterDTO(c.id, c.name, c.balance, c.profile.id, ...)
FROM GameCharacter c
```

Lazy vs eager loading:

```java
@ManyToOne(fetch = FetchType.LAZY)
private Profile profile;
```

Lazy loading avoids loading relations until needed, but can cause N+1. Eager loading loads relations immediately, but can fetch too much data. A good design uses lazy as default and query-specific fetch joins or DTO projections for endpoints that need related data.

Transaction boundary from service logic:

```java
tx.begin();

GameCharacter from = em.find(GameCharacter.class, fromId, LockModeType.OPTIMISTIC);
GameCharacter to = em.find(GameCharacter.class, toId, LockModeType.OPTIMISTIC);

from.setBalance(from.getBalance().subtract(amount));
to.setBalance(to.getBalance().add(amount));

em.persist(debitTransaction);
em.persist(creditTransaction);

tx.commit();
```

If commit fails due to optimistic locking, the code rolls back and returns conflict:

```text
409 Conflict: Balance was modified by a concurrent request. Please retry.
```

Database-level validation:

```sql
ALTER TABLE profiles
ADD CONSTRAINT uq_profiles_username UNIQUE (username);

ALTER TABLE characters
ADD CONSTRAINT fk_characters_profile
FOREIGN KEY (profile_id) REFERENCES profiles(id);
```

Application-level validation:

```java
if (quantity <= 0) {
    throw new BadRequestResponse("Quantity must be positive");
}

if (amount.compareTo(BigDecimal.ZERO) <= 0) {
    throw new BadRequestResponse("Transfer amount must be positive");
}
```

Optimistic locking model:

```java
@Version
@Column(name = "version")
private Long version;
```

Conceptual SQL:

```sql
UPDATE characters
SET balance = 900, version = version + 1
WHERE id = 42
  AND version = 3;
```

If zero rows are updated, another transaction changed the row first.

Pessimistic locking example:

```sql
BEGIN;

SELECT *
FROM characters
WHERE id = 42
FOR UPDATE;

UPDATE characters
SET balance = balance - 100
WHERE id = 42;

COMMIT;
```

Connection pooling:

```text
Without pooling:
    every request opens a new database connection
    expensive and slow

With pooling:
    application reuses a limited pool of open connections
    faster and protects database from too many connections
```

### Discuss

Data layer design is about separation of concerns. Controllers should handle HTTP, repositories should handle persistence, services should handle business transactions, and DTOs should define what crosses the API boundary.

In Bajls, the repository abstraction makes it possible to expose similar CRUD endpoints for PostgreSQL, MongoDB and Neo4j, even though the implementations are different. That is useful for comparison, but it also means each repository must respect the strengths of its database instead of forcing every database to behave exactly like PostgreSQL.

ORM is productive, but it should not be trusted blindly. Hibernate can generate good SQL, but it can also generate too many queries or fetch too much data. The practical solution is not to avoid ORM completely, but to inspect generated SQL, use DTO projections, configure lazy/eager loading carefully, and measure important endpoints.

N+1 is often a boundary problem between model configuration and query configuration. Model-level eager loading can fix one endpoint but hurt another endpoint by always loading too much. Query-level configuration, such as fetch joins or DTO projections, is usually more precise because each route can fetch exactly what it needs.

Validation should exist in both application and database. The application gives user-friendly errors and protects business workflows. The database is the final guardrail that protects data even if another script, service or bug writes invalid data.

Application-level consistency also needs idempotency and locking. Idempotency means that retrying the same request should not create duplicate effects. For example, a payment-like transfer endpoint could use an idempotency key so a repeated HTTP request does not transfer money twice. Optimistic locking fits Bajls balance updates because conflicts are possible but should be relatively rare. Pessimistic locking would be safer under high conflict, but can reduce concurrency.

Row versioning is a clean way to detect lost updates. In this project, `@Version` on `GameCharacter` means balance-changing transactions can fail safely instead of silently overwriting each other. That is a strong application-level consistency mechanism combined with database transaction behavior.

The open reasoning point is that a good data layer is not only about reading and writing rows. It controls performance, security, validation, transaction scope, API shape and consistency. The best design is explicit about where each responsibility belongs.

## 12. Stored Objects

### Explain

Stored objects er databaseobjekter, der gemmes og køres inde i databasen. De bruges til at flytte bestemte former for logik tættere på dataen. De vigtigste typer er:

- **Stored procedure**: en database-rutine, der udfører en handling.
- **Stored function**: en database-rutine, der returnerer en værdi eller tabel.
- **View**: en gemt query, der kan læses som en virtuel tabel.
- **Trigger**: kode der automatisk kører ved `INSERT`, `UPDATE` eller `DELETE`.
- **Event**: planlagt databasearbejde, der kører på et tidspunkt eller interval.

I Bajls-projektet giver stored objects mening til ting som views over characters/gangs, audit triggers, helper functions, procedures til samlede databaseoperationer og scheduled cleanup eller maintenance.

En stored procedure bruges typisk, når databasen skal udføre en handling med side effects, for eksempel opdatere flere tabeller, registrere en transaktion eller udføre en maintenance-opgave. En stored function bruges typisk, når man vil beregne og returnere noget, for eksempel antal characters for en profile eller total balance.

Et view bruges til at forenkle queries eller begrænse adgang til data. For eksempel kan et `public_profile_view` vise `id`, `username` og `role`, men skjule `password`.

En trigger bruges, når databasen automatisk skal reagere på dataændringer. For eksempel kan en trigger skrive til `audit_log`, når en character ændres.

Et event bruges til planlagte opgaver. MySQL har en indbygget event scheduler. PostgreSQL har ikke samme native event syntax, så man bruger ofte `pg_cron`, eksterne cron jobs eller application schedulers. SQL Server bruger typisk SQL Server Agent jobs.

### Compare

Fordele ved stored objects:

- tæt på dataen og kan være hurtige
- centraliserer regler på database-niveau
- kan beskytte data selv hvis flere applikationer skriver til databasen
- views kan skjule kompleksitet og sensitive kolonner
- triggers kan sikre auditing uden at applikationen husker det hver gang

Ulemper ved stored objects:

- business logic bliver spredt mellem application og database
- sværere at teste med almindelige unit tests
- kan være sværere at versionere og deploye
- kan låse projektet til én bestemt RDBMS
- triggers kan skabe skjult adfærd, som udviklere overser

Business logic i database vs application:

- Database logic er bedst til integritet, audit, constraints og regler der altid skal gælde.
- Application logic er bedst til brugerflows, API-fejlbeskeder, orchestration, eksterne services og domæneregler der ændrer sig ofte.

Comparison på tværs af RDBMS:

| Stored object | PostgreSQL | MySQL | SQL Server |
| --- | --- | --- | --- |
| Procedure | `CREATE PROCEDURE`, `CALL` | `CREATE PROCEDURE`, `CALL` | `CREATE PROCEDURE`, `EXEC` |
| Function | `CREATE FUNCTION`, PL/pgSQL | `CREATE FUNCTION` | scalar/table-valued functions |
| View | `CREATE VIEW`, materialized views findes | `CREATE VIEW` | `CREATE VIEW`, indexed views |
| Trigger | stærke row/statement triggers | row triggers | DML/DDL triggers, inserted/deleted pseudo-tables |
| Event | typisk `pg_cron` eller ekstern scheduler | native Event Scheduler | SQL Server Agent |

Stored procedure vs stored function:

- Procedure udfører typisk en handling og behøver ikke returnere en værdi.
- Function returnerer en værdi og kan ofte bruges i `SELECT`.
- Functions bør helst være mere beregnende, mens procedures ofte bruges til workflows.

View vs materialized view:

- View gemmer query-definitionen, ikke resultatet.
- Materialized view gemmer resultatet fysisk og skal refreshes.
- View er altid aktuelt, men kan være dyrt.
- Materialized view kan være hurtigt, men kan være stale.

Trigger vs application code:

- Trigger kører altid ved databaseændringen.
- Application code er mere synlig i backend-flowet.
- Trigger er god til audit og database-invariants.
- Application code er bedre, hvis handlingen kræver API context eller eksterne services.

### Demonstrate

Stored function example:

```sql
CREATE OR REPLACE FUNCTION character_count_for_profile(profile_id_input BIGINT)
RETURNS INTEGER AS $$
DECLARE
    total INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO total
    FROM characters
    WHERE profile_id = profile_id_input;

    RETURN total;
END;
$$ LANGUAGE plpgsql;
```

Use:

```sql
SELECT character_count_for_profile(10);
```

Stored procedure example:

```sql
CREATE OR REPLACE PROCEDURE transfer_balance(
    from_character_id BIGINT,
    to_character_id BIGINT,
    transfer_amount NUMERIC
)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE characters
    SET balance = balance - transfer_amount
    WHERE id = from_character_id;

    UPDATE characters
    SET balance = balance + transfer_amount
    WHERE id = to_character_id;
END;
$$;
```

Use:

```sql
CALL transfer_balance(1, 2, 100.00);
```

View example:

```sql
CREATE VIEW character_overview AS
SELECT c.id,
       c.name,
       p.username,
       h.id AS house_id,
       g.id AS garage_id
FROM characters c
JOIN profiles p ON p.id = c.profile_id
LEFT JOIN houses h ON h.character_id = c.id
LEFT JOIN garages g ON g.character_id = c.id;
```

Use:

```sql
SELECT *
FROM character_overview
WHERE username = 'player1';
```

Trigger for auditing:

```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    table_name VARCHAR(100) NOT NULL,
    operation VARCHAR(20) NOT NULL,
    row_id BIGINT,
    old_data JSONB,
    new_data JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

```sql
CREATE OR REPLACE FUNCTION audit_characters()
RETURNS trigger AS $$
BEGIN
    INSERT INTO audit_log (
        table_name,
        operation,
        row_id,
        old_data,
        new_data,
        changed_by
    )
    VALUES (
        'characters',
        TG_OP,
        COALESCE(NEW.id, OLD.id),
        to_jsonb(OLD),
        to_jsonb(NEW),
        current_user
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
```

```sql
CREATE TRIGGER trg_audit_characters
AFTER INSERT OR UPDATE OR DELETE ON characters
FOR EACH ROW
EXECUTE FUNCTION audit_characters();
```

Event-like example in PostgreSQL with cron concept:

```text
Every night at 02:00:
    run cleanup_old_audit_logs()
    refresh materialized reporting views
    verify backup status
```

MySQL event-style example:

```sql
CREATE EVENT cleanup_old_audit_logs
ON SCHEDULE EVERY 1 DAY
DO
  DELETE FROM audit_log
  WHERE changed_at < NOW() - INTERVAL 1 YEAR;
```

### Discuss

Stored objects er gode, når logikken er tæt knyttet til dataens integritet. Auditing, views, constraints-lignende checks og database maintenance er gode use cases. Til gengæld bør man være forsigtig med at flytte for meget business logic ind i databasen, fordi applikationen så bliver sværere at forstå og teste.

I Bajls giver det mening at bruge views til læsevenlige overblik og security, triggers til audit, og functions/procedures til små database-nære operationer. Men større gameplay logic, authentication flows og API behavior bør ligge i Java-koden, fordi det er nemmere at teste, versionere og returnere gode HTTP-fejl.

For MongoDB og Neo4j findes samme stored object-model ikke på samme måde som i PostgreSQL. MongoDB bruger typisk application logic, aggregation pipelines, schema validation og change streams. Neo4j bruger Cypher queries, constraints og eventuelt procedures/plugins. Derfor skal man ikke forvente, at en PostgreSQL trigger eller procedure bare kan flyttes direkte til MongoDB eller Neo4j.

Den åbne vurdering er, at stored objects er stærke værktøjer, men de skal bruges bevidst. De er bedst til regler, der absolut skal gælde tæt på dataen. Hvis reglen kræver brugercontext, eksterne services eller kompleks domænelogik, er application layer ofte det bedre sted.

## 13. Auditing

### Explain

Auditing betyder at registrere vigtige handlinger og dataændringer, så man senere kan undersøge, hvad der skete, hvem der gjorde det, hvornår det skete, og hvilken data der blev ændret. Auditing bruges til security, debugging, compliance, accountability og recovery efter fejl.

I Bajls kan auditing bruges til at spore:

- ændringer i `profiles`
- ændringer i `characters`
- balanceændringer og transactions
- gang memberships
- role changes mellem `USER` og `ADMIN`
- deletes på vigtige domain objects
- loginforsøg og administrative handlinger

En audit table bør typisk indeholde:

- audit id
- table/entity name
- operation type: `INSERT`, `UPDATE`, `DELETE`
- row id eller entity id
- old values
- new values
- changed by
- timestamp
- request id eller correlation id
- source/application navn
- eventuelt IP address eller session id

Soft delete betyder, at man ikke fysisk sletter rækken, men markerer den som slettet, for eksempel med `deleted_at` eller `is_deleted`. Audit tables gemmer derimod en historik over ændringer. De løser forskellige problemer.

Auditing er ikke det samme som logging. Logging beskriver systemhændelser og teknisk adfærd, for eksempel errors, warnings og request timing. Auditing beskriver vigtige handlinger og dataændringer, ofte med fokus på ansvar og historik.

### Compare

Soft deletes:

- Fordel: data kan skjules uden at blive fysisk slettet.
- Fordel: nemt at gendanne en slettet row.
- Ulempe: alle queries skal huske `WHERE deleted_at IS NULL`.
- Ulempe: tabeller kan vokse, og unik constraints kan blive mere komplekse.

Audit tables:

- Fordel: giver historik over ændringer.
- Fordel: kan vise old/new values.
- Fordel: virker også selvom data fysisk ændres.
- Ulempe: ekstra writes og storage.
- Ulempe: auditdata kan blive stor og kræve retention policy.

Auditing vs logging:

| Topic | Auditing | Logging |
| --- | --- | --- |
| Formål | accountability og datahistorik | drift, debugging og observability |
| Data | hvem ændrede hvad og hvornår | tekniske events, errors, timings |
| Retention | ofte længere | ofte kortere |
| Manipulation | bør være beskyttet/append-only | kan være mindre rigid |
| Eksempel | character balance changed | request took 250 ms |

Application audit vs database trigger audit:

- Application audit har mere context, for eksempel authenticated user, request id og business action.
- Database trigger audit fanger alle direkte databaseændringer, også hvis de ikke kommer fra applikationen.
- Den bedste løsning kan kombinere dem: application sender context, database sikrer at ændringen registreres.

Performance impact:

- hver audited write giver ekstra write til audit table
- storing `old_data` og `new_data` som JSON kan fylde meget
- indexes på audit table kan gøre inserts langsommere
- triggers kan øge latency på write operations
- store audit tables kræver partitioning, archiving eller retention

### Demonstrate

Audit table:

```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    operation VARCHAR(20) NOT NULL,
    old_data JSONB,
    new_data JSONB,
    changed_by VARCHAR(100),
    request_id VARCHAR(100),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Audit trigger function:

```sql
CREATE OR REPLACE FUNCTION audit_any_character_change()
RETURNS trigger AS $$
BEGIN
    INSERT INTO audit_log (
        entity_name,
        entity_id,
        operation,
        old_data,
        new_data,
        changed_by
    )
    VALUES (
        'GameCharacter',
        COALESCE(NEW.id, OLD.id),
        TG_OP,
        to_jsonb(OLD),
        to_jsonb(NEW),
        current_user
    );

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;
```

Attach trigger:

```sql
CREATE TRIGGER trg_audit_character
AFTER INSERT OR UPDATE OR DELETE ON characters
FOR EACH ROW
EXECUTE FUNCTION audit_any_character_change();
```

Soft delete:

```sql
ALTER TABLE characters
ADD COLUMN deleted_at TIMESTAMP;
```

Instead of deleting:

```sql
UPDATE characters
SET deleted_at = CURRENT_TIMESTAMP
WHERE id = 42;
```

Normal query must filter deleted rows:

```sql
SELECT *
FROM characters
WHERE deleted_at IS NULL;
```

Application-level audit pseudo-code:

```text
function transfer(fromId, toId, amount, currentUser, requestId):
    begin transaction
        update balances
        insert transaction rows
        insert audit_log(
            entity_name = "Transaction",
            operation = "TRANSFER",
            changed_by = currentUser,
            request_id = requestId
        )
    commit
```

Audit query:

```sql
SELECT operation,
       entity_name,
       entity_id,
       changed_by,
       changed_at
FROM audit_log
WHERE entity_name = 'GameCharacter'
  AND entity_id = 42
ORDER BY changed_at DESC;
```

Partition audit table by time:

```sql
CREATE TABLE audit_log_partitioned (
    id BIGSERIAL,
    changed_at TIMESTAMP NOT NULL,
    entity_name VARCHAR(100),
    operation VARCHAR(20),
    old_data JSONB,
    new_data JSONB
) PARTITION BY RANGE (changed_at);
```

### Discuss

Auditing er vigtigt, fordi systemer ikke kun skal forhindre fejl. De skal også kunne forklare fejl. Hvis en characters balance pludselig ændrer sig, skal man kunne se, om det kom fra en transfer, drug purchase, admin action eller bug.

I Bajls bør balanceændringer og `Transaction` records være særligt vigtige at auditere. `TransactionService` udfører allerede balanceændringer i en transaction, så audit-loggen bør ligge i samme transaction. På den måde commits dataændringen og audit entry sammen, eller også rulles begge tilbage.

Soft deletes og audit tables bør ikke ses som konkurrenter. Soft delete er godt, når applikationen skal kunne skjule og gendanne data. Audit table er godt, når man skal kunne se historikken bag ændringer. I mange systemer bruger man begge dele.

Auditing har en performancepris, især i write-heavy systemer. Derfor bør man auditere de vigtige ting, ikke nødvendigvis alt. Audit tables kan partitioneres efter tid, gamle audit records kan arkiveres, og man kan begrænse store JSON snapshots til sensitive tabeller eller vigtige operations.

Den åbne vurdering er, at auditing skal designes ud fra risiko. Hvis dataændringen har sikkerheds-, økonomi- eller gameplay-konsekvens, bør den auditeres. Hvis det er teknisk støj uden værdi, hører det måske bedre hjemme i almindelig logging eller metrics.

## 14. Database Modelling and Schema Design

### Explain

God database modelling handler om at forstå domainet og lave en struktur, der passer til data, regler og queries. Det løser flere problemer:

- data bliver ikke gemt tilfældigt eller dobbelt uden grund
- relationer mellem entities bliver tydelige
- business rules kan håndhæves
- queries bliver lettere at skrive og optimere
- systemet bliver nemmere at ændre og vedligeholde
- risikoen for inconsistent data bliver mindre

I Bajls-projektet betyder god modelling, at profiles, characters, houses, garages, vehicles, gangs, quests, drugs og transactions har klare relationer. For eksempel ejer en profile flere characters, en character har house/garage, og gang membership modelleres gennem `gang_affiliations`.

De tre modelniveauer har forskellige formål:

- **Conceptual model**: viser de vigtigste domain concepts og relationer uden tekniske detaljer. Her tænker man i "Profile owns Character" og "Character can join Gang".
- **Logical model**: oversætter conceptual model til en mere konkret datastruktur med entities, attributes, keys og relationer. Her begynder man at tænke i tables, foreign keys og join tables.
- **Physical model**: den faktiske databaseimplementering med datatyper, constraints, indexes, table names, column names og scripts.

ERD står for Entity Relationship Diagram. Det viser entities, attributes og relationer. Standard notationer kan være crow's foot notation, UML-lignende notation eller Chen notation. Det vigtigste er, at diagrammet viser cardinality og modality.

**Cardinality** handler om antal: one-to-one, one-to-many eller many-to-many.

**Modality** handler om obligatorisk eller valgfri deltagelse: must have eller may have.

Eksempel fra Bajls:

```text
Profile 1 ---- * Character
```

Det betyder, at én profile kan have mange characters. Hvis `character.profile_id` er `NOT NULL`, betyder det også, at en character skal have en profile.

Normalization handler om at strukturere relationelle data, så man undgår unødvendig duplication og update anomalies.

De første tre normalformer forklaret uden formelle definitioner:

- **1NF**: hver celle skal indeholde én værdi, ikke lister eller gentagne grupper. Man gemmer ikke flere vehicles i én tekstkolonne.
- **2NF**: data skal høre til hele rowens identity, ikke kun en del af en composite key. Hvis en quest title kun afhænger af `quest_id`, skal den ligge i `quests`, ikke i `character_quest`.
- **3NF**: data skal kun afhænge af den entity, tabellen beskriver. Hvis gang type afhænger af gang, skal den ligge i `gangs`, ikke gentages i `gang_affiliations`.

Normalization forebygger:

- duplicate data
- update anomalies
- insert anomalies
- delete anomalies
- inconsistent values
- uklare ownership-regler

Denormalization betyder bevidst at duplikere eller samle data for at gøre reads hurtigere eller enklere. Det bruges ofte i document databases, reporting, caching og read-heavy workloads.

Referential integrity betyder, at relationer mellem data er gyldige. I RDBMS håndhæves det med primary keys, foreign keys, `NOT NULL`, `UNIQUE`, `CHECK` og cascading rules. Et orphaned record er en row, der peger på noget, der ikke findes. For eksempel en `characters.profile_id`, der peger på en slettet profile.

Schema evolution betyder, at schemaet ændrer sig over tid, når nye krav kommer. Det kan være en ny entity, en ny relation, en ny column, ændrede constraints eller ny datamodel.

### Compare

Conceptual vs logical vs physical:

| Model | Fokus | Eksempel |
| --- | --- | --- |
| Conceptual | domain og relationer | Character joins Gang |
| Logical | struktur og keys | `gang_affiliations(character_id, gang_id)` |
| Physical | konkret DB implementation | `BIGINT`, `FOREIGN KEY`, indexes |

Normalization vs denormalization:

- Normalization er bedst til consistency, integrity og write correctness.
- Denormalization er bedst til read speed og simple aggregate queries.
- Normalization kræver ofte joins.
- Denormalization kræver mere arbejde for at holde duplicate data opdateret.

Modelling across database types:

- **RDBMS**: entities bliver tabeller, relationer bliver foreign keys eller join tables.
- **Document DB**: related data kan embeddes i documents eller references kan bruges mellem collections.
- **Graph DB**: entities bliver nodes, relationer bliver edges/relationships.

Referential integrity:

- I PostgreSQL håndhæves relationer med foreign keys.
- I MongoDB håndhæves referential integrity normalt ikke automatisk mellem collections. Det håndteres med embedding, application logic, schema validation eller transactions.
- I Neo4j kan man bruge constraints på node properties, men relationship integrity håndteres primært gennem graph writes. En relationship kan ikke eksistere uden start/end node, men domain-level referential rules skal stadig designes.

Schema migration i SQL vs NoSQL:

- SQL migrationer er typisk eksplicitte: `ALTER TABLE`, `CREATE TABLE`, `ADD CONSTRAINT`.
- NoSQL migrationer er ofte mere fleksible, fordi documents kan have forskellig shape, men production data skal stadig opdateres, hvis applikationen forventer nye felter.
- Graph migrations handler ofte om at tilføje labels, properties, constraints og nye relationship types.

Backward compatibility:

- Gamle og nye versioner af applikationen bør kunne køre under migrationen.
- Nye columns bør ofte tilføjes nullable eller med default først.
- Data backfill bør ske før man gør constraints stramme.
- API og DTOs bør kunne håndtere både gammel og ny dataform midlertidigt.

### Demonstrate

Conceptual model:

```text
Profile owns Characters.
Character owns House and Garage.
Garage contains Vehicles.
Character can join Gangs.
Character can accept Quests.
```

Logical model:

```text
profiles(id, username, email, role)
characters(id, profile_id, name, balance)
houses(id, character_id, amount_rooms)
garages(id, character_id, capacity)
vehicles(id, garage_id, model, plate_number)
gangs(id, name, type)
gang_affiliations(id, character_id, gang_id, join_date)
quests(id, title, reward)
character_quest(id, character_id, quest_id, status, accepted_at)
```

Physical model example:

```sql
CREATE TABLE characters (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_characters_profile
        FOREIGN KEY (profile_id) REFERENCES profiles(id)
);
```

Many-to-many physical model:

```sql
CREATE TABLE gang_affiliations (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL REFERENCES characters(id),
    gang_id BIGINT NOT NULL REFERENCES gangs(id),
    join_date DATE NOT NULL,
    UNIQUE (character_id, gang_id)
);
```

Adding a new entity in PostgreSQL:

```sql
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL
);

CREATE TABLE character_achievement (
    id BIGSERIAL PRIMARY KEY,
    character_id BIGINT NOT NULL REFERENCES characters(id),
    achievement_id BIGINT NOT NULL REFERENCES achievements(id),
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (character_id, achievement_id)
);
```

Adding the same concept in MongoDB:

```javascript
{
  id: 42,
  name: "ShadowKnight",
  achievements: [
    {
      id: 1,
      name: "First Win",
      unlockedAt: "2026-06-08T10:00:00"
    }
  ]
}
```

MongoDB migration/backfill:

```javascript
db.profiles.updateMany(
  { "characters.achievements": { $exists: false } },
  { $set: { "characters.$[].achievements": [] } }
)
```

Adding the same concept in Neo4j:

```cypher
CREATE CONSTRAINT achievement_id_unique
IF NOT EXISTS
FOR (a:Achievement)
REQUIRE a.id IS UNIQUE;
```

```cypher
MATCH (c:Character {id: 42})
MERGE (a:Achievement {id: 1})
SET a.name = 'First Win',
    a.description = 'Completed first win'
MERGE (c)-[:UNLOCKED {unlockedAt: '2026-06-08T10:00:00'}]->(a);
```

Backward-compatible SQL migration:

```sql
-- Step 1: add nullable column
ALTER TABLE profiles
ADD COLUMN display_name VARCHAR(100);

-- Step 2: backfill existing data
UPDATE profiles
SET display_name = username
WHERE display_name IS NULL;

-- Step 3: add constraint after data is valid
ALTER TABLE profiles
ALTER COLUMN display_name SET NOT NULL;
```

Production-safe migration flow:

```text
1. Deploy schema change that is backward compatible.
2. Backfill existing production data in batches.
3. Deploy application code that uses the new schema.
4. Verify data and monitor errors.
5. Remove old columns/logic later in a separate migration.
```

Migration between database technologies:

```text
PostgreSQL source snapshot
    -> load profiles, characters, houses, garages, vehicles, gangs, quests
    -> transform into MongoDB profile documents
    -> transform into Neo4j nodes and relationships
    -> verify counts and sample queries
```

This is the same general idea as the Bajls migration flow, where PostgreSQL is the source and MongoDB/Neo4j are rebuilt as projections.

### Discuss

God modelling starter ikke med databaseproduktet. Det starter med domainet og spørgsmålene systemet skal svare på. I Bajls er domainet stærkt relationelt, så PostgreSQL er god som source of truth. Men nogle læsemønstre passer bedre til MongoDB, og nogle relationelle spørgsmål passer bedre til Neo4j.

Normalization er især vigtig i PostgreSQL, fordi den beskytter mod duplication og inconsistency. Men hvis en API-route ofte skal hente en hel profile med alle characters og relaterede data, kan en denormalized MongoDB projection give bedre performance. Det er ikke et tegn på, at normalization er forkert. Det betyder bare, at write model og read model kan have forskellige behov.

Referential integrity er stærkest og mest eksplicit i RDBMS. I document databases undgår man nogle referential integrity problemer ved at embedde data, men hvis man bruger references mellem collections, skal applikationen ofte selv forhindre orphaned references. I graph databases er relationer første klasse, men domain constraints skal stadig tænkes igennem.

Når et nyt krav ændrer schemaet, bør man ikke bare ændre databasen direkte i production. Man bør lave en migration plan, tænke backward compatibility, håndtere eksisterende data, teste restore/rollback, og måle data migration cost. Store data migrations kan være dyre, fordi de bruger CPU, disk I/O, locks, storage og tid.

NoSQL betyder ikke "ingen schema". Det betyder ofte, at schemaet er mere fleksibelt og ligger mere i applikationen. MongoDB documents kan have forskellig shape, men API'et og repositories forventer stadig bestemte felter. Derfor kræver NoSQL schema evolution også planlægning, defaults, backfills og compatibility.

Migration mellem database technologies er mere end at kopiere rows. Man ændrer datamodel. PostgreSQL foreign keys og join tables kan blive embedded arrays i MongoDB eller relationships i Neo4j. Derfor skal migrationen både transformere data og validere, at den nye model stadig kan svare på de vigtige queries.

Den åbne vurdering er, at schema design altid er et kompromis mellem correctness, performance, simplicity og future change. Et godt schema gør det let at håndhæve regler i dag, men også muligt at udvikle systemet, når nye krav kommer.
