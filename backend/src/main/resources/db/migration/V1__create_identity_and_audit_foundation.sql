-- PostgreSQL executes this Flyway migration transactionally. Keep all DDL transactional.

CREATE TABLE app_user (
    id uuid NOT NULL,
    email varchar(320) NOT NULL,
    password_hash varchar(255) NOT NULL,
    display_name varchar(200) NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    failed_login_count integer NOT NULL DEFAULT 0,
    locked_until timestamptz NULL,
    last_login_at timestamptz NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT ck_app_user_email_not_blank CHECK (btrim(email) <> ''),
    CONSTRAINT ck_app_user_password_hash_not_blank CHECK (btrim(password_hash) <> ''),
    CONSTRAINT ck_app_user_display_name_not_blank CHECK (btrim(display_name) <> ''),
    CONSTRAINT ck_app_user_failed_login_count_nonnegative CHECK (failed_login_count >= 0),
    CONSTRAINT fk_app_user_created_by FOREIGN KEY (created_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION,
    CONSTRAINT fk_app_user_updated_by FOREIGN KEY (updated_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION,
    CONSTRAINT fk_app_user_deleted_by FOREIGN KEY (deleted_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION
);

CREATE UNIQUE INDEX ux_app_user_email_active_ci
    ON app_user (lower(email))
    WHERE deleted_at IS NULL;

CREATE TABLE role (
    id uuid NOT NULL,
    code varchar(64) NOT NULL,
    description varchar(500) NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_role PRIMARY KEY (id),
    CONSTRAINT uq_role_code UNIQUE (code),
    CONSTRAINT ck_role_code_uppercase_identifier
        CHECK (code ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT fk_role_created_by FOREIGN KEY (created_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION,
    CONSTRAINT fk_role_updated_by FOREIGN KEY (updated_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION,
    CONSTRAINT fk_role_deleted_by FOREIGN KEY (deleted_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION
);

-- Stable system role identifiers. These rows intentionally have no user or credentials.
INSERT INTO role (
    id,
    code,
    description,
    is_active,
    created_at,
    updated_at,
    version
) VALUES
    (
        '00000000-0000-0000-0000-000000000001',
        'ADMIN',
        'Administrative access role.',
        true,
        TIMESTAMPTZ '2026-01-01 00:00:00+00',
        TIMESTAMPTZ '2026-01-01 00:00:00+00',
        0
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        'SUPER_ADMIN',
        'Site-owner administrative access role.',
        true,
        TIMESTAMPTZ '2026-01-01 00:00:00+00',
        TIMESTAMPTZ '2026-01-01 00:00:00+00',
        0
    );

CREATE TABLE user_role (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL,
    assigned_at timestamptz NOT NULL,
    assigned_by uuid NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id)
        REFERENCES app_user (id) ON DELETE RESTRICT ON UPDATE NO ACTION,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id)
        REFERENCES role (id) ON DELETE RESTRICT ON UPDATE NO ACTION,
    CONSTRAINT fk_user_role_assigned_by FOREIGN KEY (assigned_by)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION
);

CREATE INDEX ix_user_role_role_id_user_id
    ON user_role (role_id, user_id);

CREATE TABLE audit_event (
    id uuid NOT NULL,
    occurred_at timestamptz NOT NULL,
    actor_user_id uuid NULL,
    action varchar(100) NOT NULL,
    target_type varchar(100) NOT NULL,
    target_id uuid NULL,
    outcome varchar(20) NOT NULL,
    request_id varchar(128) NULL,
    ip_address inet NULL,
    details jsonb NOT NULL DEFAULT '{}'::jsonb,
    CONSTRAINT pk_audit_event PRIMARY KEY (id),
    CONSTRAINT ck_audit_event_action_not_blank CHECK (btrim(action) <> ''),
    CONSTRAINT ck_audit_event_target_type_not_blank CHECK (btrim(target_type) <> ''),
    CONSTRAINT ck_audit_event_outcome
        CHECK (outcome IN ('SUCCESS', 'FAILURE', 'DENIED')),
    CONSTRAINT ck_audit_event_request_id_not_blank
        CHECK (request_id IS NULL OR btrim(request_id) <> ''),
    CONSTRAINT ck_audit_event_details_object CHECK (jsonb_typeof(details) = 'object'),
    CONSTRAINT fk_audit_event_actor_user FOREIGN KEY (actor_user_id)
        REFERENCES app_user (id) ON DELETE SET NULL ON UPDATE NO ACTION
);

CREATE INDEX ix_audit_event_occurred_at_desc
    ON audit_event (occurred_at DESC);

CREATE INDEX ix_audit_event_actor_user_id_occurred_at_desc
    ON audit_event (actor_user_id, occurred_at DESC);

CREATE INDEX ix_audit_event_target_type_target_id_occurred_at_desc
    ON audit_event (target_type, target_id, occurred_at DESC);

CREATE INDEX ix_audit_event_request_id
    ON audit_event (request_id)
    WHERE request_id IS NOT NULL;
