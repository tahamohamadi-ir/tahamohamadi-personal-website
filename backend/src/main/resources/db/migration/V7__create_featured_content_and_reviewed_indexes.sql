CREATE TABLE featured_item (
    id uuid NOT NULL, slot_key varchar(100) NOT NULL, blog_post_id uuid NULL,
    portfolio_project_id uuid NULL, publication_id uuid NULL, sort_order integer NOT NULL,
    starts_at timestamptz NULL, ends_at timestamptz NULL, is_active boolean NOT NULL DEFAULT true,
    created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL, created_by uuid NULL,
    updated_by uuid NULL, deleted_at timestamptz NULL, deleted_by uuid NULL, version bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_featured_item PRIMARY KEY(id), CONSTRAINT ck_featured_item_slot CHECK(btrim(slot_key)<>''),
    CONSTRAINT ck_featured_item_order CHECK(sort_order>=0),
    CONSTRAINT ck_featured_item_target CHECK((blog_post_id IS NOT NULL)::integer + (portfolio_project_id IS NOT NULL)::integer + (publication_id IS NOT NULL)::integer = 1),
    CONSTRAINT ck_featured_item_window CHECK(ends_at IS NULL OR starts_at IS NULL OR ends_at >= starts_at),
    CONSTRAINT fk_featured_item_blog_post FOREIGN KEY(blog_post_id) REFERENCES blog_post(id) ON DELETE RESTRICT,
    CONSTRAINT fk_featured_item_project FOREIGN KEY(portfolio_project_id) REFERENCES portfolio_project(id) ON DELETE RESTRICT,
    CONSTRAINT fk_featured_item_publication FOREIGN KEY(publication_id) REFERENCES publication(id) ON DELETE RESTRICT,
    CONSTRAINT fk_featured_item_created_by FOREIGN KEY(created_by) REFERENCES app_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_featured_item_updated_by FOREIGN KEY(updated_by) REFERENCES app_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_featured_item_deleted_by FOREIGN KEY(deleted_by) REFERENCES app_user(id) ON DELETE SET NULL
);
CREATE UNIQUE INDEX ux_featured_item_slot_order_active ON featured_item(slot_key,sort_order) WHERE is_active AND deleted_at IS NULL;
CREATE INDEX ix_featured_item_active_window_order ON featured_item(slot_key,is_active,starts_at,ends_at,sort_order,id);
CREATE INDEX ix_content_page_admin_updated ON content_page(updated_at DESC,id) WHERE deleted_at IS NULL;
CREATE INDEX ix_portfolio_project_admin_updated ON portfolio_project(updated_at DESC,id) WHERE deleted_at IS NULL;
CREATE INDEX ix_publication_admin_updated ON publication(updated_at DESC,id) WHERE deleted_at IS NULL;
