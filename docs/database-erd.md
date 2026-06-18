# Database ERD

## Entity Relationship Diagram

```mermaid
erDiagram
    users ||--o{ user_oauth_links : "has (ADULT)"
    users ||--o{ family_guardians : "joins"
    users ||--o| children : "logs in as (CHILD)"
    users ||--o{ family_join_requests : "requests"
    users ||--o{ family_join_requests : "reviews"
    users ||--o{ families : "creates"
    users ||--o{ schedules : "creates"

    families ||--o{ family_guardians : "has"
    families ||--o{ family_join_requests : "has"
    families ||--o{ children : "has"

    children ||--o| child_credentials : "PIN"
    children ||--o{ child_device_sessions : "devices"
    children ||--o{ schedules : "has"

    users {
        uuid id PK
        varchar nickname
        varchar account_type "ADULT|CHILD"
        timestamptz created_at
        timestamptz updated_at
    }

    user_oauth_links {
        uuid id PK
        uuid user_id FK
        varchar oauth_provider
        varchar oauth_subject
        timestamptz created_at
        timestamptz updated_at
    }

    families {
        uuid id PK
        varchar name
        uuid created_by FK
        timestamptz created_at
        timestamptz updated_at
    }

    family_guardians {
        uuid id PK
        uuid family_id FK
        uuid user_id FK
        varchar role "MASTER|FAMILY|HELPER"
        boolean can_edit
        timestamptz created_at
        timestamptz updated_at
    }

    family_join_requests {
        uuid id PK
        uuid family_id FK
        uuid user_id FK
        varchar role
        boolean can_edit
        varchar invite_code_hash
        varchar status "PENDING|APPROVED|REJECTED|EXPIRED"
        uuid reviewed_by FK
        timestamptz reviewed_at
        timestamptz created_at
        timestamptz updated_at
    }

    children {
        uuid id PK
        uuid family_id FK
        varchar nickname
        smallint birth_year
        uuid user_id FK "nullable until paired"
        boolean can_edit
        boolean app_enabled
        timestamptz created_at
        timestamptz updated_at
    }

    child_credentials {
        uuid child_id PK,FK
        varchar pin_hash
        smallint failed_attempts
        timestamptz locked_until
        timestamptz created_at
        timestamptz updated_at
    }

    child_device_sessions {
        uuid id PK
        uuid child_id FK
        varchar device_token_hash
        varchar device_name
        timestamptz last_active_at
        timestamptz expires_at
        timestamptz created_at
        timestamptz updated_at
    }

    schedules {
        uuid id PK
        uuid child_id FK
        varchar title
        varchar description
        varchar schedule_type "DROPOFF|PICKUP|ACTIVITY|OTHER"
        timestamptz start_at
        timestamptz end_at
        uuid created_by FK
        timestamptz created_at
        timestamptz updated_at
    }
```

## Redis (not in RDB)

| Key | Value | TTL |
|-----|-------|-----|
| `invite:{code}` | `{ familyId, role, canEdit, invitedBy }` | 24h |
| `pair:{code}` | `{ childId, familyId, issuedBy }` | 5–10min |

## Flyway migrations

| Version | File |
|---------|------|
| V1 | `V1__create_users_and_oauth_links.sql` |
| V2 | `V2__create_families_and_guardians.sql` |
| V3 | `V3__create_family_join_requests.sql` |
| V4 | `V4__create_children_and_auth.sql` |
| V5 | `V5__create_schedules.sql` |

## JPA entities

Package: `com.kidschedule.api.domain.entity`

- `User`, `UserOauthLink`
- `Family`, `FamilyGuardian`, `FamilyJoinRequest`
- `Child`, `ChildCredential`, `ChildDeviceSession`
- `Schedule`

Enums: `com.kidschedule.api.domain.enums`
