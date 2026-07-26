ALTER TABLE blog_post
    ADD COLUMN source_language varchar(2) NOT NULL DEFAULT 'fa',
    ADD CONSTRAINT ck_blog_post_source_language CHECK (source_language IN ('fa', 'en'));
