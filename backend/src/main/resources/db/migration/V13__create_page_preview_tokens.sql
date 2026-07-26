CREATE TABLE content_page_preview_token (
    id uuid NOT NULL,
    content_page_id uuid NOT NULL,
    token_hash char(64) NOT NULL,
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    created_by uuid NULL,
    CONSTRAINT pk_content_page_preview_token PRIMARY KEY (id),
    CONSTRAINT uq_content_page_preview_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_content_page_preview_token_page FOREIGN KEY (content_page_id) REFERENCES content_page (id) ON DELETE CASCADE,
    CONSTRAINT fk_content_page_preview_token_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_content_page_preview_token_lookup ON content_page_preview_token (token_hash, expires_at);
