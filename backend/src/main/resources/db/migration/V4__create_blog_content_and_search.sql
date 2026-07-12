CREATE TABLE blog_category (
    id uuid NOT NULL,
    category_key varchar(100) NOT NULL,
    sort_order integer NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_blog_category PRIMARY KEY (id),
    CONSTRAINT uq_blog_category_key UNIQUE (category_key),
    CONSTRAINT ck_blog_category_key_not_blank CHECK (btrim(category_key) <> ''),
    CONSTRAINT ck_blog_category_sort_order_nonnegative CHECK (sort_order >= 0),
    CONSTRAINT fk_blog_category_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_category_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_category_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_blog_category_active_order ON blog_category (is_active, sort_order, id);

CREATE TABLE blog_category_translation (
    id uuid NOT NULL,
    blog_category_id uuid NOT NULL,
    language_code varchar(2) NOT NULL,
    name varchar(255) NOT NULL,
    slug varchar(255) NOT NULL,
    seo_title varchar(255) NULL,
    seo_description varchar(500) NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_blog_category_translation PRIMARY KEY (id),
    CONSTRAINT uq_blog_category_translation_locale UNIQUE (blog_category_id, language_code),
    CONSTRAINT ck_blog_category_translation_locale CHECK (language_code IN ('fa', 'en')),
    CONSTRAINT ck_blog_category_translation_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_blog_category_translation_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT fk_blog_category_translation_category FOREIGN KEY (blog_category_id) REFERENCES blog_category (id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_category_translation_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_category_translation_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_category_translation_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX ux_blog_category_translation_locale_slug_active_ci ON blog_category_translation (language_code, lower(slug)) WHERE deleted_at IS NULL;
CREATE INDEX ix_blog_category_translation_category_locale ON blog_category_translation (blog_category_id, language_code);

CREATE TABLE blog_post (
    id uuid NOT NULL,
    author_user_id uuid NULL,
    category_id uuid NOT NULL,
    cover_media_id uuid NULL,
    status varchar(20) NOT NULL,
    published_at timestamptz NULL,
    scheduled_for timestamptz NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_blog_post PRIMARY KEY (id),
    CONSTRAINT ck_blog_post_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_blog_post_published_at CHECK (status <> 'PUBLISHED' OR published_at IS NOT NULL),
    CONSTRAINT fk_blog_post_author FOREIGN KEY (author_user_id) REFERENCES app_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_blog_post_category FOREIGN KEY (category_id) REFERENCES blog_category (id) ON DELETE RESTRICT,
    CONSTRAINT fk_blog_post_cover_media FOREIGN KEY (cover_media_id) REFERENCES media_asset (id) ON DELETE RESTRICT,
    CONSTRAINT fk_blog_post_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_post_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_post_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_blog_post_status_published_at_desc ON blog_post (status, published_at DESC, id DESC);
CREATE INDEX ix_blog_post_category_status_published_at_desc ON blog_post (category_id, status, published_at DESC, id DESC);
CREATE INDEX ix_blog_post_author ON blog_post (author_user_id) WHERE author_user_id IS NOT NULL;
CREATE INDEX ix_blog_post_updated_at_desc ON blog_post (updated_at DESC, id DESC);

CREATE TABLE blog_post_translation (
    id uuid NOT NULL,
    blog_post_id uuid NOT NULL,
    language_code varchar(2) NOT NULL,
    title varchar(255) NOT NULL,
    slug varchar(255) NOT NULL,
    excerpt text NULL,
    body_markdown text NOT NULL,
    seo_title varchar(255) NULL,
    seo_description varchar(500) NULL,
    search_vector tsvector GENERATED ALWAYS AS (
        to_tsvector(CASE WHEN language_code = 'en' THEN 'english'::regconfig ELSE 'simple'::regconfig END,
            coalesce(title, '') || ' ' || coalesce(excerpt, '') || ' ' || coalesce(body_markdown, ''))
    ) STORED,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_blog_post_translation PRIMARY KEY (id),
    CONSTRAINT uq_blog_post_translation_locale UNIQUE (blog_post_id, language_code),
    CONSTRAINT ck_blog_post_translation_locale CHECK (language_code IN ('fa', 'en')),
    CONSTRAINT ck_blog_post_translation_title_not_blank CHECK (btrim(title) <> ''),
    CONSTRAINT ck_blog_post_translation_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT ck_blog_post_translation_body_not_blank CHECK (btrim(body_markdown) <> ''),
    CONSTRAINT fk_blog_post_translation_post FOREIGN KEY (blog_post_id) REFERENCES blog_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_post_translation_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_post_translation_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_blog_post_translation_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX ux_blog_post_translation_locale_slug_active_ci ON blog_post_translation (language_code, lower(slug)) WHERE deleted_at IS NULL;
CREATE INDEX ix_blog_post_translation_post_locale ON blog_post_translation (blog_post_id, language_code);
CREATE INDEX ix_blog_post_translation_search_vector ON blog_post_translation USING gin (search_vector);

CREATE TABLE tag (
    id uuid NOT NULL,
    tag_key varchar(100) NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_tag PRIMARY KEY (id),
    CONSTRAINT uq_tag_key UNIQUE (tag_key),
    CONSTRAINT ck_tag_key_not_blank CHECK (btrim(tag_key) <> ''),
    CONSTRAINT fk_tag_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_tag_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_tag_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_tag_active ON tag (is_active, id);

CREATE TABLE tag_translation (
    id uuid NOT NULL,
    tag_id uuid NOT NULL,
    language_code varchar(2) NOT NULL,
    name varchar(255) NOT NULL,
    slug varchar(255) NOT NULL,
    seo_title varchar(255) NULL,
    seo_description varchar(500) NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_tag_translation PRIMARY KEY (id),
    CONSTRAINT uq_tag_translation_locale UNIQUE (tag_id, language_code),
    CONSTRAINT ck_tag_translation_locale CHECK (language_code IN ('fa', 'en')),
    CONSTRAINT ck_tag_translation_name_not_blank CHECK (btrim(name) <> ''),
    CONSTRAINT ck_tag_translation_slug_not_blank CHECK (btrim(slug) <> ''),
    CONSTRAINT fk_tag_translation_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE,
    CONSTRAINT fk_tag_translation_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_tag_translation_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_tag_translation_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX ux_tag_translation_locale_slug_active_ci ON tag_translation (language_code, lower(slug)) WHERE deleted_at IS NULL;
CREATE INDEX ix_tag_translation_tag_locale ON tag_translation (tag_id, language_code);

CREATE TABLE blog_post_tag (
    blog_post_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    CONSTRAINT pk_blog_post_tag PRIMARY KEY (blog_post_id, tag_id),
    CONSTRAINT fk_blog_post_tag_post FOREIGN KEY (blog_post_id) REFERENCES blog_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_post_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE RESTRICT
);
CREATE INDEX ix_blog_post_tag_tag_post ON blog_post_tag (tag_id, blog_post_id);

CREATE TABLE blog_post_media (
    blog_post_id uuid NOT NULL,
    media_asset_id uuid NOT NULL,
    usage varchar(20) NOT NULL,
    sort_order integer NOT NULL,
    CONSTRAINT pk_blog_post_media PRIMARY KEY (blog_post_id, media_asset_id, usage),
    CONSTRAINT uq_blog_post_media_usage_order UNIQUE (blog_post_id, usage, sort_order),
    CONSTRAINT ck_blog_post_media_usage CHECK (usage IN ('ATTACHMENT', 'INLINE')),
    CONSTRAINT ck_blog_post_media_sort_order_nonnegative CHECK (sort_order >= 0),
    CONSTRAINT fk_blog_post_media_post FOREIGN KEY (blog_post_id) REFERENCES blog_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_post_media_asset FOREIGN KEY (media_asset_id) REFERENCES media_asset (id) ON DELETE RESTRICT
);
CREATE INDEX ix_blog_post_media_post_usage_order ON blog_post_media (blog_post_id, usage, sort_order);
CREATE INDEX ix_blog_post_media_asset_post ON blog_post_media (media_asset_id, blog_post_id);
