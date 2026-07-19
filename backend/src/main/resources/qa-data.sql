-- Local QA-only data. This file is loaded only when the explicit qa profile is active.
-- The fixed IDs and primary-key conflict handling make repeated profile starts idempotent.

INSERT INTO content_page (id, page_key, status, published_at, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000101', 'home', 'PUBLISHED', '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000102', 'about', 'PUBLISHED', '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000103', 'research', 'PUBLISHED', '2026-01-01T00:00:00Z', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO content_page_translation (id, content_page_id, language_code, title, slug, summary, body_markdown, seo_title, seo_description, canonical_path, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000111', '00000000-0000-0000-0000-000000000101', 'en', 'QA Home English', 'qa-home-en', 'Neutral local QA home content.', 'This is isolated local QA content.', 'QA Home English', 'Neutral local QA home.', '/en', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000112', '00000000-0000-0000-0000-000000000101', 'fa', 'Ã˜Â®Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’', 'qa-home-fa', 'Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§Ã›Å’ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', 'Ã˜Â§Ã›Å’Ã™â€  Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§ Ã™ÂÃ™â€šÃ˜Â· Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’ Ã˜Â§Ã˜Â³Ã˜Âª.', 'Ã˜Â®Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’', 'Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§Ã›Å’ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', '/fa', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000121', '00000000-0000-0000-0000-000000000102', 'en', 'QA About English', 'about', 'Neutral local QA about content.', 'This page confirms the local managed-page route.', 'QA About English', 'Neutral local QA about.', '/en/about', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000122', '00000000-0000-0000-0000-000000000102', 'fa', 'Ã˜Â¯Ã˜Â±Ã˜Â¨Ã˜Â§Ã˜Â±Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’', 'about', 'Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§Ã›Å’ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¯Ã˜Â±Ã˜Â¨Ã˜Â§Ã˜Â±Ã™â€¡ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', 'Ã˜Â§Ã›Å’Ã™â€  Ã˜ÂµÃ™ÂÃ˜Â­Ã™â€¡ Ã™â€¦Ã˜Â³Ã›Å’Ã˜Â± Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’ Ã˜ÂµÃ™ÂÃ˜Â­Ã™â€¡ Ã™â€¦Ã˜Â¯Ã›Å’Ã˜Â±Ã›Å’Ã˜ÂªÃ¢â‚¬Å’Ã˜Â´Ã˜Â¯Ã™â€¡ Ã˜Â±Ã˜Â§ Ã˜Â¨Ã˜Â±Ã˜Â±Ã˜Â³Ã›Å’ Ã™â€¦Ã›Å’Ã¢â‚¬Å’ÃšÂ©Ã™â€ Ã˜Â¯.', 'Ã˜Â¯Ã˜Â±Ã˜Â¨Ã˜Â§Ã˜Â±Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’', 'Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§Ã›Å’ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¯Ã˜Â±Ã˜Â¨Ã˜Â§Ã˜Â±Ã™â€¡ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', '/fa/about', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000131', '00000000-0000-0000-0000-000000000103', 'en', 'QA Research English', 'research', 'Neutral local QA research content.', 'This is isolated local QA content.', 'QA Research English', 'Neutral local QA research.', '/en/research', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000132', '00000000-0000-0000-0000-000000000103', 'fa', 'Ã™Â¾ÃšËœÃ™Ë†Ã™â€¡Ã˜Â´ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’', 'research', 'Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§Ã›Å’ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã™Â¾ÃšËœÃ™Ë†Ã™â€¡Ã˜Â´ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', 'Ã˜Â§Ã›Å’Ã™â€  Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§ Ã™ÂÃ™â€šÃ˜Â· Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’ Ã˜Â§Ã˜Â³Ã˜Âª.', 'Ã™Â¾ÃšËœÃ™Ë†Ã™â€¡Ã˜Â´ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’', 'Ã™â€¦Ã˜Â­Ã˜ÂªÃ™Ë†Ã˜Â§Ã›Å’ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã™Â¾ÃšËœÃ™Ë†Ã™â€¡Ã˜Â´ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', '/fa/research', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO blog_category (id, category_key, sort_order, is_active, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000201', 'qa-category', 0, true, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO blog_category_translation (id, blog_category_id, language_code, name, slug, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000211', '00000000-0000-0000-0000-000000000201', 'en', 'QA Category', 'qa-category', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000212', '00000000-0000-0000-0000-000000000201', 'fa', 'Ã˜Â¯Ã˜Â³Ã˜ÂªÃ™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'qa-category-fa', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO blog_post (id, category_id, status, published_at, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000221', '00000000-0000-0000-0000-000000000201', 'PUBLISHED', '2026-01-02T00:00:00Z', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000222', '00000000-0000-0000-0000-000000000201', 'PUBLISHED', '2026-01-03T00:00:00Z', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO blog_post_translation (id, blog_post_id, language_code, title, slug, excerpt, body_markdown, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000231', '00000000-0000-0000-0000-000000000221', 'en', 'QA Bilingual Blog', 'qa-blog-bilingual', 'Neutral English QA blog excerpt.', 'Neutral English QA blog body.', 'QA Bilingual Blog', 'Neutral English QA blog.' , TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000232', '00000000-0000-0000-0000-000000000221', 'fa', 'Ã˜Â¨Ã™â€žÃ˜Â§ÃšÂ¯ Ã˜Â¯Ã™Ë†Ã˜Â²Ã˜Â¨Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'qa-blog-bilingual-fa', 'Ã˜Â®Ã™â€žÃ˜Â§Ã˜ÂµÃ™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã™â€žÃ˜Â§ÃšÂ¯ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', 'Ã™â€¦Ã˜ÂªÃ™â€  Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã™â€žÃ˜Â§ÃšÂ¯ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', 'Ã˜Â¨Ã™â€žÃ˜Â§ÃšÂ¯ Ã˜Â¯Ã™Ë†Ã˜Â²Ã˜Â¨Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã˜Â®Ã™â€žÃ˜Â§Ã˜ÂµÃ™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã™â€žÃ˜Â§ÃšÂ¯ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000233', '00000000-0000-0000-0000-000000000222', 'en', 'QA Missing Translation', 'qa-missing-translation', 'English-only local QA resource.', 'This resource intentionally has no Persian translation.', 'QA Missing Translation', 'English-only local QA resource.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO skill_category (id, category_key, sort_order, is_active, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000301', 'qa-skills', 0, true, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO skill_category_translation (id, skill_category_id, language_code, name, description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000311', '00000000-0000-0000-0000-000000000301', 'en', 'QA Skills', 'Neutral local QA skill category.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000312', '00000000-0000-0000-0000-000000000301', 'fa', 'Ã™â€¦Ã™â€¡Ã˜Â§Ã˜Â±Ã˜ÂªÃ¢â‚¬Å’Ã™â€¡Ã˜Â§Ã›Å’ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã˜Â¯Ã˜Â³Ã˜ÂªÃ™â€¡ Ã™â€¦Ã™â€¡Ã˜Â§Ã˜Â±Ã˜Âª Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO skill (id, skill_key, skill_category_id, sort_order, is_active, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000321', 'qa-skill', '00000000-0000-0000-0000-000000000301', 0, true, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO skill_translation (id, skill_id, language_code, name, description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000331', '00000000-0000-0000-0000-000000000321', 'en', 'QA Skill', 'Neutral local QA skill.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000332', '00000000-0000-0000-0000-000000000321', 'fa', 'Ã™â€¦Ã™â€¡Ã˜Â§Ã˜Â±Ã˜Âª Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã™â€¦Ã™â€¡Ã˜Â§Ã˜Â±Ã˜Âª Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO portfolio_project (id, project_key, status, started_on, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000401', 'qa-portfolio-bilingual', 'PUBLISHED', '2026-01-01', 0, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO portfolio_project_translation (id, portfolio_project_id, language_code, title, slug, summary, body_markdown, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000411', '00000000-0000-0000-0000-000000000401', 'en', 'QA Bilingual Portfolio', 'qa-portfolio-bilingual', 'Neutral English QA portfolio summary.', 'Neutral English QA portfolio body.', 'QA Bilingual Portfolio', 'Neutral English QA portfolio.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000412', '00000000-0000-0000-0000-000000000401', 'fa', 'Ã™â€ Ã™â€¦Ã™Ë†Ã™â€ Ã™â€¡Ã¢â‚¬Å’ÃšÂ©Ã˜Â§Ã˜Â± Ã˜Â¯Ã™Ë†Ã˜Â²Ã˜Â¨Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'qa-portfolio-bilingual-fa', 'Ã˜Â®Ã™â€žÃ˜Â§Ã˜ÂµÃ™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã™â€ Ã™â€¦Ã™Ë†Ã™â€ Ã™â€¡Ã¢â‚¬Å’ÃšÂ©Ã˜Â§Ã˜Â± Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', 'Ã™â€¦Ã˜ÂªÃ™â€  Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã™â€ Ã™â€¦Ã™Ë†Ã™â€ Ã™â€¡Ã¢â‚¬Å’ÃšÂ©Ã˜Â§Ã˜Â± Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', 'Ã™â€ Ã™â€¦Ã™Ë†Ã™â€ Ã™â€¡Ã¢â‚¬Å’ÃšÂ©Ã˜Â§Ã˜Â± Ã˜Â¯Ã™Ë†Ã˜Â²Ã˜Â¨Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã˜Â®Ã™â€žÃ˜Â§Ã˜ÂµÃ™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã™â€ Ã™â€¦Ã™Ë†Ã™â€ Ã™â€¡Ã¢â‚¬Å’ÃšÂ©Ã˜Â§Ã˜Â± Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO portfolio_project_skill (portfolio_project_id, skill_id, sort_order) VALUES
  ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000321', 0)
ON CONFLICT (portfolio_project_id, skill_id) DO NOTHING;

INSERT INTO publication (id, publication_key, content_status, publication_stage, year, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000501', 'qa-publication-bilingual', 'PUBLISHED', 'PUBLISHED', 2026, 0, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO publication_translation (id, publication_id, language_code, title, slug, abstract_text, authors_display, venue_display, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000511', '00000000-0000-0000-0000-000000000501', 'en', 'QA Bilingual Publication', 'qa-publication-bilingual', 'Neutral English QA publication abstract.', 'QA Author', 'QA Venue', 'QA Bilingual Publication', 'Neutral English QA publication.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000512', '00000000-0000-0000-0000-000000000501', 'fa', 'Ã˜Â§Ã™â€ Ã˜ÂªÃ˜Â´Ã˜Â§Ã˜Â± Ã˜Â¯Ã™Ë†Ã˜Â²Ã˜Â¨Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'qa-publication-bilingual-fa', 'Ãšâ€ ÃšÂ©Ã›Å’Ã˜Â¯Ã™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â§Ã™â€ Ã˜ÂªÃ˜Â´Ã˜Â§Ã˜Â± Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', 'Ã™â€ Ã™Ë†Ã›Å’Ã˜Â³Ã™â€ Ã˜Â¯Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã™â€ Ã˜Â´Ã˜Â±Ã›Å’Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã˜Â§Ã™â€ Ã˜ÂªÃ˜Â´Ã˜Â§Ã˜Â± Ã˜Â¯Ã™Ë†Ã˜Â²Ã˜Â¨Ã˜Â§Ã™â€ Ã™â€¡ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ãšâ€ ÃšÂ©Ã›Å’Ã˜Â¯Ã™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â§Ã™â€ Ã˜ÂªÃ˜Â´Ã˜Â§Ã˜Â± Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’ Ã™ÂÃ˜Â§Ã˜Â±Ã˜Â³Ã›Å’.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO publication (id, publication_key, content_status, publication_stage, year, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000502', 'qa-publication-en-only', 'PUBLISHED', 'PUBLISHED', 2026, 1, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO publication_translation (id, publication_id, language_code, title, slug, abstract_text, authors_display, venue_display, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000521', '00000000-0000-0000-0000-000000000502', 'en', 'QA English-only Publication', 'qa-publication-en-only', 'Neutral English-only QA publication abstract.', 'QA Author', 'QA Venue', 'QA English-only Publication', 'Neutral English-only QA publication.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO resume_entry (id, entry_type, status, started_on, ended_on, is_current, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000601', 'RESEARCH', 'PUBLISHED', '2025-01-01', NULL, true, 0, TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;

INSERT INTO resume_entry_translation (id, resume_entry_id, language_code, title, organization, location, summary, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000611', '00000000-0000-0000-0000-000000000601', 'en', 'QA Research Entry', 'QA Organization', 'Local', 'Neutral local QA resume entry.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0),
  ('00000000-0000-0000-0000-000000000612', '00000000-0000-0000-0000-000000000601', 'fa', 'Ã˜Â³Ã˜Â§Ã˜Â¨Ã™â€šÃ™â€¡ Ã™Â¾ÃšËœÃ™Ë†Ã™â€¡Ã˜Â´Ã›Å’ Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã˜Â³Ã˜Â§Ã˜Â²Ã™â€¦Ã˜Â§Ã™â€  Ã˜Â¢Ã˜Â²Ã™â€¦Ã˜Â§Ã›Å’Ã˜Â´Ã›Å’', 'Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’', 'Ã˜Â³Ã˜Â§Ã˜Â¨Ã™â€šÃ™â€¡ Ã˜Â®Ã™â€ Ã˜Â«Ã›Å’ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ ÃšÂ©Ã™â€ Ã˜ÂªÃ˜Â±Ã™â€ž ÃšÂ©Ã›Å’Ã™ÂÃ›Å’ Ã™â€¦Ã˜Â­Ã™â€žÃ›Å’.', TIMESTAMPTZ '2026-01-01 00:00:00+00', TIMESTAMPTZ '2026-01-01 00:00:00+00', 0)
ON CONFLICT (id) DO UPDATE SET created_at = EXCLUDED.created_at, updated_at = EXCLUDED.updated_at;
