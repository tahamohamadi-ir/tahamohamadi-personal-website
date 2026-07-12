CREATE TABLE media_asset (
    id uuid NOT NULL,
    storage_key varchar(255) NOT NULL,
    original_filename varchar(255) NOT NULL,
    extension varchar(32) NOT NULL,
    mime_type varchar(255) NOT NULL,
    size_bytes bigint NOT NULL,
    checksum_sha256 varchar(64) NOT NULL,
    width integer NULL,
    height integer NULL,
    status varchar(20) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_media_asset PRIMARY KEY (id),
    CONSTRAINT uq_media_asset_storage_key UNIQUE (storage_key),
    CONSTRAINT ck_media_asset_storage_key_not_blank CHECK (btrim(storage_key) <> ''),
    CONSTRAINT ck_media_asset_original_filename_not_blank CHECK (btrim(original_filename) <> ''),
    CONSTRAINT ck_media_asset_extension_not_blank CHECK (btrim(extension) <> ''),
    CONSTRAINT ck_media_asset_mime_type_not_blank CHECK (btrim(mime_type) <> ''),
    CONSTRAINT ck_media_asset_size_positive CHECK (size_bytes > 0),
    CONSTRAINT ck_media_asset_checksum_sha256 CHECK (checksum_sha256 ~ '^[A-Fa-f0-9]{64}$'),
    CONSTRAINT ck_media_asset_dimensions_nonnegative CHECK ((width IS NULL OR width >= 0) AND (height IS NULL OR height >= 0)),
    CONSTRAINT ck_media_asset_status CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT fk_media_asset_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_media_asset_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_media_asset_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE INDEX ix_media_asset_checksum_sha256 ON media_asset (checksum_sha256);
CREATE INDEX ix_media_asset_status_created_at_desc ON media_asset (status, created_at DESC, id DESC);
CREATE INDEX ix_media_asset_created_by ON media_asset (created_by) WHERE created_by IS NOT NULL;

CREATE TABLE media_asset_translation (
    id uuid NOT NULL,
    media_asset_id uuid NOT NULL,
    language_code varchar(2) NOT NULL,
    alt_text varchar(500) NOT NULL,
    caption text NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_media_asset_translation PRIMARY KEY (id),
    CONSTRAINT uq_media_asset_translation_locale UNIQUE (media_asset_id, language_code),
    CONSTRAINT ck_media_asset_translation_locale CHECK (language_code IN ('fa', 'en')),
    CONSTRAINT ck_media_asset_translation_alt_length CHECK (char_length(alt_text) <= 500),
    CONSTRAINT fk_media_asset_translation_asset FOREIGN KEY (media_asset_id) REFERENCES media_asset (id) ON DELETE CASCADE,
    CONSTRAINT fk_media_asset_translation_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_media_asset_translation_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_media_asset_translation_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);

CREATE INDEX ix_media_asset_translation_asset_locale ON media_asset_translation (media_asset_id, language_code);
