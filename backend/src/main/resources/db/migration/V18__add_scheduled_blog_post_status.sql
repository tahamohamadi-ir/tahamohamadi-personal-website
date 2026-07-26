ALTER TABLE blog_post DROP CONSTRAINT ck_blog_post_status;
ALTER TABLE blog_post ADD CONSTRAINT ck_blog_post_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'));
ALTER TABLE blog_post ADD CONSTRAINT ck_blog_post_scheduled_for CHECK (status <> 'SCHEDULED' OR scheduled_for IS NOT NULL);
CREATE INDEX ix_blog_post_scheduled_publish ON blog_post (scheduled_for, id) WHERE status = 'SCHEDULED' AND deleted_at IS NULL;
