CREATE TABLE content_page_section (
    id uuid NOT NULL,
    content_page_id uuid NOT NULL,
    section_type varchar(64) NOT NULL DEFAULT 'STANDARD',
    layout varchar(64) NOT NULL DEFAULT 'SINGLE_COLUMN',
    sort_order integer NOT NULL,
    is_enabled boolean NOT NULL DEFAULT true,
    settings_json text NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    created_by uuid NULL,
    updated_by uuid NULL,
    deleted_at timestamptz NULL,
    deleted_by uuid NULL,
    version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_content_page_section PRIMARY KEY (id),
    CONSTRAINT ck_content_page_section_type CHECK (section_type IN ('STANDARD')),
    CONSTRAINT ck_content_page_section_layout CHECK (layout IN ('SINGLE_COLUMN')),
    CONSTRAINT ck_content_page_section_order CHECK (sort_order >= 0),
    CONSTRAINT fk_content_page_section_page FOREIGN KEY (content_page_id) REFERENCES content_page (id) ON DELETE CASCADE,
    CONSTRAINT fk_content_page_section_created_by FOREIGN KEY (created_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_content_page_section_updated_by FOREIGN KEY (updated_by) REFERENCES app_user (id) ON DELETE SET NULL,
    CONSTRAINT fk_content_page_section_deleted_by FOREIGN KEY (deleted_by) REFERENCES app_user (id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX ux_content_page_section_page_order ON content_page_section (content_page_id, sort_order) WHERE deleted_at IS NULL;
CREATE INDEX ix_content_page_section_page_order ON content_page_section (content_page_id, sort_order, id) WHERE deleted_at IS NULL;

ALTER TABLE content_page_block ADD COLUMN content_page_section_id uuid NULL;

-- A page ID is a stable deterministic ID for the compatibility section. This
-- avoids synthetic UUID generation in a data migration and makes reruns safe.
INSERT INTO content_page_section (id, content_page_id, section_type, layout, sort_order, is_enabled, created_at, updated_at, version)
SELECT page.id, page.id, 'STANDARD', 'SINGLE_COLUMN', 0, true, page.created_at, page.updated_at, 0
FROM content_page page
WHERE EXISTS (
    SELECT 1 FROM content_page_block block
    WHERE block.content_page_id = page.id AND block.deleted_at IS NULL
)
ON CONFLICT (id) DO NOTHING;

UPDATE content_page_block
SET content_page_section_id = content_page_id
WHERE content_page_section_id IS NULL;

ALTER TABLE content_page_block
    ALTER COLUMN content_page_section_id SET NOT NULL,
    ADD CONSTRAINT fk_content_page_block_section FOREIGN KEY (content_page_section_id) REFERENCES content_page_section (id) ON DELETE CASCADE;

ALTER TABLE content_page_block DROP CONSTRAINT uq_content_page_block_order;
ALTER TABLE content_page_block ADD CONSTRAINT uq_content_page_block_section_order UNIQUE (content_page_section_id, sort_order);
CREATE INDEX ix_content_page_block_section_order ON content_page_block (content_page_section_id, sort_order, id) WHERE deleted_at IS NULL;
