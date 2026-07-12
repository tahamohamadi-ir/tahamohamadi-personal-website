CREATE TABLE content_page (
    id uuid NOT NULL,
    page_key varchar(100) NOT NULL,
    status varchar(20) NOT NULL,
    published_at timestamptz NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_content_page PRIMARY KEY (id),
    CONSTRAINT uq_content_page_page_key UNIQUE (page_key),
    CONSTRAINT ck_content_page_key_not_blank CHECK (btrim(page_key) <> ''),
    CONSTRAINT ck_content_page_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_content_page_published_at CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL),
    CONSTRAINT fk_content_page_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_content_page_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_content_page_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_content_page_status_published_at_desc ON content_page (status, published_at DESC, id DESC);
CREATE INDEX ix_content_page_updated_at_desc ON content_page (updated_at DESC, id DESC);

CREATE TABLE content_page_translation (
    id uuid NOT NULL,
    content_page_id uuid NOT NULL,
    language_code varchar(2) NOT NULL,
    title varchar(255) NOT NULL,
    slug varchar(255) NOT NULL,
    summary text NULL,
    body_markdown text NULL,
    seo_title varchar(255) NULL,
    seo_description varchar(500) NULL,
    canonical_path varchar(500) NULL,
    og_media_id uuid NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_content_page_translation PRIMARY KEY (id),
    CONSTRAINT uq_content_page_translation_locale UNIQUE (content_page_id, language_code),
    CONSTRAINT ck_content_page_translation_locale CHECK (language_code IN ('fa', 'en')),
    CONSTRAINT ck_content_page_translation_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT ck_content_page_translation_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT fk_content_page_translation_page FOREIGN KEY (content_page_id) REFERENCES content_page (id) ON DELETE CASCADE,
    CONSTRAINT fk_content_page_translation_og_media FOREIGN KEY (og_media_id) REFERENCES media_asset (id) ON DELETE RESTRICT,
    CONSTRAINT fk_content_page_translation_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_content_page_translation_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_content_page_translation_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX ux_content_page_translation_locale_slug_active_ci ON content_page_translation (language_code, lower(slug)) WHERE deleted_at IS NULL;
CREATE INDEX ix_content_page_translation_page_locale ON content_page_translation (content_page_id, language_code);

CREATE TABLE social_link (
    id uuid NOT NULL,
    platform_code varchar(64) NOT NULL,
    url varchar(2048) NOT NULL,
    sort_order integer NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_social_link PRIMARY KEY (id),
    CONSTRAINT uq_social_link_platform_code UNIQUE (platform_code),
    CONSTRAINT ck_social_link_platform_code_not_blank CHECK (btrim(platform_code) <> ''),
    CONSTRAINT ck_social_link_url_scheme CHECK (url ~* '^(https?://|mailto:)'),
    CONSTRAINT ck_social_link_sort_order_nonnegative CHECK (sort_order >= 0),
    CONSTRAINT fk_social_link_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_social_link_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_social_link_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_social_link_active_order ON social_link (is_active, sort_order, id);

CREATE TABLE contact_message (
    id uuid NOT NULL,
    sender_name varchar(200) NOT NULL,
    sender_email varchar(320) NOT NULL,
    message text NOT NULL,
    source_language varchar(2) NOT NULL,
    status varchar(20) NOT NULL,
    submitted_at timestamptz NOT NULL,
    read_at timestamptz NULL,
    archived_at timestamptz NULL,
    CONSTRAINT pk_contact_message PRIMARY KEY (id),
    CONSTRAINT ck_contact_message_name_not_blank CHECK (btrim(sender_name) <> ''),
    CONSTRAINT ck_contact_message_email_not_blank CHECK (btrim(sender_email) <> ''),
    CONSTRAINT ck_contact_message_message_not_blank CHECK (btrim(message) <> ''),
    CONSTRAINT ck_contact_message_message_length CHECK (char_length(message) <= 10000),
    CONSTRAINT ck_contact_message_locale CHECK (source_language IN ('fa', 'en')),
    CONSTRAINT ck_contact_message_status CHECK (status IN ('NEW', 'READ', 'ARCHIVED'))
);
CREATE INDEX ix_contact_message_status_submitted_at_desc ON contact_message (status, submitted_at DESC, id DESC);
CREATE INDEX ix_contact_message_sender_email ON contact_message (sender_email);
CREATE INDEX ix_contact_message_submitted_at_desc ON contact_message (submitted_at DESC, id DESC);
