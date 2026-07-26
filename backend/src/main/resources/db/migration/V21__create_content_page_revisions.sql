CREATE TABLE content_page_revision (
    id uuid NOT NULL,
    content_page_id uuid NOT NULL,
    revision_number integer NOT NULL,
    snapshot_json text NOT NULL,
    reason varchar(40) NOT NULL,
    created_at timestamptz NOT NULL,
    created_by uuid NULL,
    CONSTRAINT pk_content_page_revision PRIMARY KEY (id),
    CONSTRAINT uq_content_page_revision_number UNIQUE (content_page_id, revision_number),
    CONSTRAINT ck_content_page_revision_number_positive CHECK (revision_number > 0),
    CONSTRAINT ck_content_page_revision_snapshot_not_blank CHECK (btrim(snapshot_json) <> ''),
    CONSTRAINT fk_content_page_revision_page FOREIGN KEY (content_page_id) REFERENCES content_page (id) ON DELETE CASCADE,
    CONSTRAINT fk_content_page_revision_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_content_page_revision_page_number_desc ON content_page_revision (content_page_id, revision_number DESC);
