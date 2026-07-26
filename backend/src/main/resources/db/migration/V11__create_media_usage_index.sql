CREATE TABLE media_usage (
    media_asset_id uuid NOT NULL,
    owner_type varchar(64) NOT NULL,
    owner_id uuid NOT NULL,
    reference_key varchar(64) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT pk_media_usage PRIMARY KEY (media_asset_id, owner_type, owner_id, reference_key),
    CONSTRAINT fk_media_usage_asset FOREIGN KEY (media_asset_id) REFERENCES media_asset (id) ON DELETE CASCADE
);

CREATE INDEX ix_media_usage_owner ON media_usage (owner_type, owner_id);
CREATE INDEX ix_media_usage_asset ON media_usage (media_asset_id, owner_type, owner_id);

INSERT INTO media_usage (media_asset_id, owner_type, owner_id, reference_key)
SELECT translation.og_media_id, 'PAGE_OPEN_GRAPH', page.id, 'OPEN_GRAPH'
FROM content_page_translation translation
JOIN content_page page ON page.id = translation.content_page_id
WHERE translation.og_media_id IS NOT NULL AND translation.deleted_at IS NULL AND page.deleted_at IS NULL
UNION ALL
SELECT setting.logo_media_id, 'SETTINGS_LOGO', setting.id, 'LOGO'
FROM site_setting setting
WHERE setting.logo_media_id IS NOT NULL AND setting.deleted_at IS NULL
UNION ALL
SELECT setting.og_media_id, 'SETTINGS_OPEN_GRAPH', setting.id, 'OPEN_GRAPH'
FROM site_setting setting
WHERE setting.og_media_id IS NOT NULL AND setting.deleted_at IS NULL
UNION ALL
SELECT (match.captures)[1]::uuid, 'COMPOSER_BLOCK', block.id, 'mediaId'
FROM content_page_block block
CROSS JOIN LATERAL regexp_matches(coalesce(block.settings_json, ''), '"mediaId"\\s*:\\s*"([0-9a-fA-F-]{36})"', 'g') AS match(captures)
JOIN media_asset asset ON asset.id = (match.captures)[1]::uuid
WHERE block.deleted_at IS NULL
UNION ALL
SELECT post.cover_media_id, 'BLOG_COVER', post.id, 'COVER'
FROM blog_post post
WHERE post.cover_media_id IS NOT NULL AND post.deleted_at IS NULL
UNION ALL
SELECT media.media_asset_id, CASE WHEN media.usage = 'INLINE' THEN 'BLOG_INLINE' ELSE 'BLOG_ATTACHMENT' END, media.blog_post_id, media.usage::text
FROM blog_post_media media
JOIN blog_post post ON post.id = media.blog_post_id
WHERE post.deleted_at IS NULL
UNION ALL
SELECT project.cover_media_id, 'PORTFOLIO_COVER', project.id, 'COVER'
FROM portfolio_project project
WHERE project.cover_media_id IS NOT NULL AND project.deleted_at IS NULL
UNION ALL
SELECT publication.cover_media_id, 'PUBLICATION_COVER', publication.id, 'COVER'
FROM publication
WHERE publication.cover_media_id IS NOT NULL AND publication.deleted_at IS NULL
UNION ALL
SELECT document.media_asset_id, 'RESUME_DOCUMENT', document.id, 'DOCUMENT'
FROM resume_document document
WHERE document.deleted_at IS NULL
ON CONFLICT DO NOTHING;
