CREATE TABLE blog_post_revision (
    id uuid NOT NULL,
    blog_post_id uuid NOT NULL,
    revision_number integer NOT NULL,
    snapshot_json text NOT NULL,
    reason varchar(40) NOT NULL,
    created_at timestamptz NOT NULL,
    created_by uuid NULL,
    CONSTRAINT pk_blog_post_revision PRIMARY KEY (id),
    CONSTRAINT uq_blog_post_revision_number UNIQUE (blog_post_id, revision_number),
    CONSTRAINT ck_blog_post_revision_number_positive CHECK (revision_number > 0),
    CONSTRAINT ck_blog_post_revision_snapshot_not_blank CHECK (btrim(snapshot_json) <> ''),
    CONSTRAINT fk_blog_post_revision_post FOREIGN KEY (blog_post_id) REFERENCES blog_post (id) ON DELETE CASCADE,
    CONSTRAINT fk_blog_post_revision_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE INDEX ix_blog_post_revision_post_number_desc ON blog_post_revision (blog_post_id, revision_number DESC);
