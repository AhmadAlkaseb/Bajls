---
header-includes:
  - \usepackage{fancyhdr}
  - \pagestyle{fancy}
  - \fancyhf{}
  - \fancyfoot[R]{\thepage}
  - \renewcommand{\headrulewidth}{0pt}
  - \renewcommand{\footrulewidth}{0pt}
---

<div align="center">

# RPG Game Database Design
### Final Version

**Prepared by Group 1**

Ahmad Abdel Razak Hussein Alkaseb  
Benjamin Sebastian Barrales Hernandez  
Jeppe Ronning Koch  
Laith Abdel Razak Hussein Alkaseb  
Sadek Alsukafi

</div>

---

| **Course** | Database |
|---|---|
| **Project** | RPG Game by Bajls |
| **Date of delivery** | *(To be filled in)* |
| **List of figures** | *(To be filled in)* |
| **List of appendices** | *(To be filled in)* |

\thispagestyle{empty}
\newpage

## 1. Introduction

Our goal is to design and develop a next-generation RPG game inspired by
the creative freedom and social interaction found in platforms like
Roblox. The game allows players to explore an interactive world,
customize their characters in detail, and engage in dynamic gameplay
experiences.

At its core, the game revolves around a customizable character that can
move freely in a large open world. Players experience the game through
their character, whose appearance, identity, and status are visible to
others in real time. As in Roblox-inspired games, visual identity and
personalization are central to the player experience.

The first development phase focuses on character customization and
player identity. Players create a personal profile and design one or
more unique characters with specific physical traits and attributes.
These traits define how the character looks and can later support social
interactions and additional gameplay mechanics.

The world is structured around exploration, housing, and social systems
such as gangs. Every character is stored in a structured database that
maintains logical relationships between profiles, characters, roles, and
properties. The system enforces clear rules for ownership, identity, and
mandatory attributes.

The player experience includes:

-   Creating a personal profile
-   Customizing characters with physical traits
-   Exploring the map
-   Owning a house
-   Optionally joining a gang
-   Interacting with other players

The game architecture must support both regular players and
administrators who manage the system.

------------------------------------------------------------------------

## 2. Requirements

This section defines the functional requirements for our RPG game
inspired by Roblox. The purpose is to clearly define persons, profiles,
characters, and their relationships in the system. The system must
enforce strict rules to ensure data integrity, logical consistency, and
reliable gameplay functionality.

### Profile and Role Requirements

#### Person and Profile

-   A person can have exactly one profile in the system.
-   The profile represents the player's digital identity.
-   A profile must be uniquely identifiable (for example by `profile_id`
    or `username`).
-   A person is not allowed to create multiple profiles.

This ensures that each real-world player is connected to one controlled
game identity.

#### Role System

-   A profile must have exactly one role.
-   The role can be either `User` or `Admin`.
-   A profile cannot have multiple roles at the same time.
-   `Admin` accounts have extended permissions compared to `User`
    accounts, such as management and moderation functionality.

This establishes role-based access control in the game.

### Character Requirements

#### Profile to Character Relationship

-   A profile can have one or more characters.
-   A character must belong to exactly one profile.
-   A character cannot exist without being linked to a profile.

This creates a one-to-many relationship between `Profile` and
`Character`.

#### Character Attributes

Each character must have exactly one of each required attribute:

-   One gender
-   One weight
-   One height
-   One eye color
-   One skin color

All attributes are mandatory and cannot be empty. A character cannot
have multiple values for any of these attributes. The system must
validate that all required attributes are present before a character can
be created.

### Housing Requirement

-   A character must have exactly one house.
-   A character cannot exist without an assigned house.
-   A house belongs to exactly one character.
-   A house cannot be shared by multiple characters.

This creates a mandatory one-to-one relationship between `Character` and
`House`. The system must prevent characters without houses and houses
without assigned characters.

### Gang Membership Requirement

-   Gang membership is optional.
-   A character can belong to zero or more gangs.
-   A gang can have zero or more characters.

This establishes an optional many-to-many relationship between
`Character` and `Gang`.

### Data Integrity Rules

The system must enforce the following constraints:

-   Every person has at most one profile.
-   Every profile has exactly one role.
-   Every character belongs to exactly one profile.
-   Every character has all required physical attributes.
-   Every character has exactly one house.
-   A character may belong to zero or more gangs.

The system must prevent invalid states such as orphan characters,
missing houses, missing roles, or duplicate role assignments.

### Relationship Summary

-   `Person` to `Profile`: One-to-One
-   `Profile` to `Role`: One-to-One
-   `Profile` to `Character`: One-to-Many
-   `Character` to `House`: One-to-One (Mandatory)
-   `Character` to `Gang`: Optional Many-to-Many
-   `Character` to Attributes: Exactly one value per required attribute

### Conclusion

These requirements define the structural foundation of the RPG system.
They ensure clear identity management, controlled permissions, complete
character definitions, and mandatory housing ownership. This
specification forms the basis for conceptual, logical, and physical
database modeling.

------------------------------------------------------------------------

## 2.2.1. Entity/Relationship Model (Conceptual -> Logical -> Physical model)

This section presents the entity/relationship progression from
conceptual understanding to logical structure, and prepares the
transition to the physical implementation in Section 2.3.

### Conceptual Model

The conceptual model describes the business domain without technical
implementation details such as data types, indexes, or SQL syntax. Its
purpose is to show what information the system must manage and how core
concepts are related from a game and business perspective. In this
project, `Characters` is the central concept because most game actions,
ownership rules, and social interactions are represented through
character entities.

### Conceptual Diagram

![Conceptual data model](images/conceptual-model-2026-02-10.png)

The model includes the following core entities:

-   **Profiles**: the persistent player identity used for login and
    account-level ownership.
-   **Roles**: access-level definitions (`User` and `Admin`) connected
    to profile permissions.
-   **Characters**: playable identities controlled by profiles inside
    the game world.
-   **Houses**: character-owned properties used to represent private
    assets and ownership constraints.
-   **Gangs**: social groups that support optional membership and
    multi-character affiliation.
-   **Genders, Weights, Heights, Eyecolors, Skincolors**: controlled
    attribute domains used to define mandatory character appearance
    traits.

### Key Relationships in the Diagram

-   `Profiles` to `Roles`: many-to-one.
    Each profile has exactly one role, while one role can be assigned to
    many profiles.
-   `Profiles` to `Characters`: one-to-many.
    A profile can own multiple characters, and each character belongs to
    one profile.
-   `Characters` to `Houses`: one-to-one (mandatory).
    Each character must be linked to one house, and each house belongs
    to one character.
-   `Characters` to appearance tables (`Genders`, `Weights`, `Heights`,
    `Eyecolors`, `Skincolors`): many-to-one for each attribute.
    Each character has exactly one value per attribute, and many
    characters can share the same attribute value.
-   `Characters` to `Gangs`: optional many-to-many.
    A character may join zero or more gangs, and each gang may contain
    multiple characters.

The conceptual model also defines participation rules. Participation is
mandatory for `Character` to `Profile`, `Character` to `House`, and
`Character` to each appearance category because these are required for
valid gameplay state. Participation is optional for gang membership
because not all players must engage in group-based social mechanics.

From a domain perspective, this model prevents ambiguous ownership. A
profile owns characters, characters own houses, and social membership is
kept independent through gangs. This separation is important because
each area has different lifecycle rules: deleting or deactivating a
profile affects owned characters, while gang membership can be added or
removed without changing character identity.

The conceptual view therefore acts as the foundation for the next
models. It communicates business meaning to both technical and
non-technical stakeholders, verifies that rules are complete before
implementation, and reduces redesign risk later in the project.

### Logical Model

The logical model translates the conceptual design into a normalized
relational structure. At this level, entities are expressed as tables,
relationships are expressed using primary and foreign keys, and business
rules are transformed into enforceable structural constraints. The
logical model remains DBMS-independent in principle, but it is precise
enough to be implemented consistently.

### Logical Diagram

![Logical data model](images/logical-model-2026-02-10.png)

### Main Tables

-   **Profiles** (`profile_id` PK, first_name, last_name, email,
    username, password, `role_id` FK)
-   **Roles** (`role_id` PK, name)
-   **Characters** (`character_id` PK, name, balance, `profile_id` FK,
    `gender_id` FK, `weight_id` FK, `height_id` FK, `eyecolor_id` FK,
    `skincolor_id` FK)
-   **Houses** (`house_id` PK, amount_rooms, amount_bathrooms,
    `character_id` FK UNIQUE)
-   **Gangs** (`gang_id` PK, name, type)
-   **Gang_Affiliations** (`character_id` FK, `gang_id` FK, join_date,
    composite PK)
-   **Genders, Weights, Heights, Eyecolors, Skincolors**
    (`id` PK, `name`)

The table layout intentionally separates high-change data from
low-change reference data. `characters` is transaction-heavy and
contains gameplay-relevant state, while lookup tables hold stable
classification values. This design reduces redundancy and makes updates
predictable. For example, changing a lookup label does not require
editing every character row.

### Logical Relationship Rules

-   One `Profile` belongs to one `Role`; one `Role` can be used by many
    profiles.
-   One `Profile` can own many `Characters`.
-   Each `Character` must reference exactly one value in each attribute
    lookup table.
-   `Character` and `House` are modeled as a one-to-one relation via a
    unique foreign key.
-   `Character` and `Gang` are modeled through the bridge table
    `Gang_Affiliations` (many-to-many).

In addition to these cardinalities, the logical model defines key
strategy and uniqueness boundaries:

-   Surrogate keys (`id`) are used for stable identity across all main
    tables.
-   `username` should be unique at profile level to guarantee a single
    login identity per player.
-   `houses.character_id` is unique to enforce one house per character.
-   `gang_affiliations` uses a composite key (`character_id`, `gang_id`)
    to prevent duplicate membership records.

This structure supports both correctness and scalability. As the number
of players grows, relationships remain clear and query paths remain
deterministic. Typical queries, such as "all characters for profile X"
or "all gangs for character Y," map directly to key-based joins.
Logical clarity at this stage is critical because it minimizes ambiguity
before physical optimization in PostgreSQL.

------------------------------------------------------------------------

## 2.2.2. Normalization process

Normalization is applied to ensure that data is stored once, updated in
one place, and queried without contradictions. The goal is to eliminate
redundancy and avoid insertion, update, and deletion anomalies that
would otherwise appear in gameplay data.

### First Normal Form (1NF)

1NF requires atomic attributes and no repeating groups in a single
column. In this schema, all tables satisfy 1NF because each column holds
one value per row. For example, a character does not store multiple eye
colors or multiple gang IDs in one field. Multi-valued relationships are
modeled through separate tables (especially `gang_affiliations`).

### Second Normal Form (2NF)

2NF requires that non-key attributes depend on the full primary key, not
part of it. This is especially relevant in tables with composite keys.
`gang_affiliations` satisfies 2NF because `join_date` depends on the
specific combination of `character_id` and `gang_id` rather than only
one of them. No partial dependency is present.

### Third Normal Form (3NF)

3NF removes transitive dependencies where non-key attributes depend on
other non-key attributes. The schema satisfies this by separating roles,
appearance categories, and gangs into dedicated tables referenced by
foreign keys. For example, role names are not duplicated in `profiles`,
and appearance labels are not duplicated in `characters`.

The resulting 3NF design improves consistency and maintainability:

-   Changes are localized to one table.
-   Duplicate text values are minimized.
-   Integrity constraints become easier to enforce.
-   Query semantics remain stable as data volume grows.

The final logical schema therefore satisfies **3NF** and provides a
reliable base for physical implementation.

------------------------------------------------------------------------

## 2.3. Physical data model

The physical model is the concrete PostgreSQL implementation of the
logical schema. At this stage, abstract relations are converted into
actual database objects with column types, constraints, and execution
characteristics. The implementation shown in pgAdmin reflects the final
table structure and foreign-key network used by the project.

### Physical Diagram

![Physical data model in PostgreSQL (pgAdmin)](images/physical-model-2026-02-10.png)

### Implementation Details

-   Primary keys are implemented as integer identifiers (`id`).
-   Foreign keys are created between dependent tables to enforce
    referential integrity.
-   The `houses.character_id` column is unique, enforcing one house per
    character.
-   The `gang_affiliations` table stores many-to-many links between
    characters and gangs, including `join_date`.
-   Lookup tables (`genders`, `weights`, `heights`, `eyecolors`,
    `skincolors`, `roles`) reduce redundancy and centralize valid
    values.

The PostgreSQL schema is designed to enforce business rules at database
level, not only in application code. Mandatory fields are protected with
`NOT NULL`, key relationships are protected with foreign keys, and
entity identity is protected with primary-key constraints. This reduces
the risk of inconsistent data even if multiple services or scripts write
to the same database.

For relationship-heavy queries, indexing strategy is important. Primary
keys are indexed automatically, and foreign-key columns should be
indexed to improve join performance, especially for frequent operations
such as loading profile characters, character attributes, or gang
memberships. As data volume increases, this prevents full-table scans
for common gameplay queries.

Operationally, the physical design also supports maintainability:

-   Clear naming conventions for tables and columns.
-   Dedicated bridge table for many-to-many associations.
-   Stable lookup tables for controlled enumerations.
-   Predictable join paths for reporting and administration tools.

### Data Types

-   `INTEGER` for primary and foreign keys
-   `VARCHAR(20)` for names and short text values
-   `REAL` for character balance
-   `DATE` for `join_date` in `gang_affiliations`
-   `NOT NULL` on mandatory columns

These choices balance simplicity and correctness. Integer keys are fast
for joins, short varchar fields are sufficient for constrained labels,
and date storage supports timeline analysis of gang membership. If
future requirements demand larger values or stricter validation, the
model can be extended with wider text constraints, check constraints,
and additional indexes without breaking the current structure.
