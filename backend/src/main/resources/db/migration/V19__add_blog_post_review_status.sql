ALTER TABLE blog_post DROP CONSTRAINT ck_blog_post_status;
ALTER TABLE blog_post ADD CONSTRAINT ck_blog_post_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'));
