-- Local QA-only data. This file is loaded only when the explicit qa profile is active.
-- The fixed IDs and primary-key conflict handling make repeated profile starts idempotent.

INSERT INTO content_page (id, page_key, status, published_at, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000101', 'home', 'PUBLISHED', '2026-01-01T00:00:00Z', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000102', 'about', 'PUBLISHED', '2026-01-01T00:00:00Z', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000103', 'research', 'PUBLISHED', '2026-01-01T00:00:00Z', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO content_page_translation (id, content_page_id, language_code, title, slug, summary, body_markdown, seo_title, seo_description, canonical_path, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000111', '00000000-0000-0000-0000-000000000101', 'en', 'QA Home English', 'qa-home-en', 'Neutral local QA home content.', 'This is isolated local QA content.', 'QA Home English', 'Neutral local QA home.', '/en', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000112', '00000000-0000-0000-0000-000000000101', 'fa', 'خانه آزمایشی فارسی', 'qa-home-fa', 'محتوای خنثی برای کنترل کیفی محلی.', 'این محتوا فقط برای کنترل کیفی محلی است.', 'خانه آزمایشی فارسی', 'محتوای خنثی برای کنترل کیفی محلی.', '/fa', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000121', '00000000-0000-0000-0000-000000000102', 'en', 'QA About English', 'about', 'Neutral local QA about content.', 'This page confirms the local managed-page route.', 'QA About English', 'Neutral local QA about.', '/en/about', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000122', '00000000-0000-0000-0000-000000000102', 'fa', 'درباره آزمایشی فارسی', 'about', 'محتوای خنثی درباره برای کنترل کیفی محلی.', 'این صفحه مسیر محلی صفحه مدیریت‌شده را بررسی می‌کند.', 'درباره آزمایشی فارسی', 'محتوای خنثی درباره برای کنترل کیفی محلی.', '/fa/about', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000131', '00000000-0000-0000-0000-000000000103', 'en', 'QA Research English', 'research', 'Neutral local QA research content.', 'This is isolated local QA content.', 'QA Research English', 'Neutral local QA research.', '/en/research', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000132', '00000000-0000-0000-0000-000000000103', 'fa', 'پژوهش آزمایشی فارسی', 'research', 'محتوای خنثی پژوهش برای کنترل کیفی محلی.', 'این محتوا فقط برای کنترل کیفی محلی است.', 'پژوهش آزمایشی فارسی', 'محتوای خنثی پژوهش برای کنترل کیفی محلی.', '/fa/research', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO blog_category (id, category_key, sort_order, is_active, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000201', 'qa-category', 0, true, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO blog_category_translation (id, blog_category_id, language_code, name, slug, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000211', '00000000-0000-0000-0000-000000000201', 'en', 'QA Category', 'qa-category', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000212', '00000000-0000-0000-0000-000000000201', 'fa', 'دسته آزمایشی', 'qa-category-fa', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO blog_post (id, category_id, status, published_at, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000221', '00000000-0000-0000-0000-000000000201', 'PUBLISHED', '2026-01-02T00:00:00Z', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000222', '00000000-0000-0000-0000-000000000201', 'PUBLISHED', '2026-01-03T00:00:00Z', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO blog_post_translation (id, blog_post_id, language_code, title, slug, excerpt, body_markdown, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000231', '00000000-0000-0000-0000-000000000221', 'en', 'QA Bilingual Blog', 'qa-blog-bilingual', 'Neutral English QA blog excerpt.', 'Neutral English QA blog body.', 'QA Bilingual Blog', 'Neutral English QA blog.' , now(), now(), 0),
  ('00000000-0000-0000-0000-000000000232', '00000000-0000-0000-0000-000000000221', 'fa', 'بلاگ دوزبانه آزمایشی', 'qa-blog-bilingual-fa', 'خلاصه خنثی بلاگ آزمایشی فارسی.', 'متن خنثی بلاگ آزمایشی فارسی.', 'بلاگ دوزبانه آزمایشی', 'خلاصه خنثی بلاگ آزمایشی فارسی.', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000233', '00000000-0000-0000-0000-000000000222', 'en', 'QA Missing Translation', 'qa-missing-translation', 'English-only local QA resource.', 'This resource intentionally has no Persian translation.', 'QA Missing Translation', 'English-only local QA resource.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO skill_category (id, category_key, sort_order, is_active, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000301', 'qa-skills', 0, true, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO skill_category_translation (id, skill_category_id, language_code, name, description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000311', '00000000-0000-0000-0000-000000000301', 'en', 'QA Skills', 'Neutral local QA skill category.', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000312', '00000000-0000-0000-0000-000000000301', 'fa', 'مهارت‌های آزمایشی', 'دسته مهارت خنثی برای کنترل کیفی محلی.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO skill (id, skill_key, skill_category_id, sort_order, is_active, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000321', 'qa-skill', '00000000-0000-0000-0000-000000000301', 0, true, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO skill_translation (id, skill_id, language_code, name, description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000331', '00000000-0000-0000-0000-000000000321', 'en', 'QA Skill', 'Neutral local QA skill.', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000332', '00000000-0000-0000-0000-000000000321', 'fa', 'مهارت آزمایشی', 'مهارت خنثی برای کنترل کیفی محلی.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO portfolio_project (id, project_key, status, started_on, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000401', 'qa-portfolio-bilingual', 'PUBLISHED', '2026-01-01', 0, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO portfolio_project_translation (id, portfolio_project_id, language_code, title, slug, summary, body_markdown, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000411', '00000000-0000-0000-0000-000000000401', 'en', 'QA Bilingual Portfolio', 'qa-portfolio-bilingual', 'Neutral English QA portfolio summary.', 'Neutral English QA portfolio body.', 'QA Bilingual Portfolio', 'Neutral English QA portfolio.', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000412', '00000000-0000-0000-0000-000000000401', 'fa', 'نمونه‌کار دوزبانه آزمایشی', 'qa-portfolio-bilingual-fa', 'خلاصه خنثی نمونه‌کار آزمایشی فارسی.', 'متن خنثی نمونه‌کار آزمایشی فارسی.', 'نمونه‌کار دوزبانه آزمایشی', 'خلاصه خنثی نمونه‌کار آزمایشی فارسی.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO portfolio_project_skill (portfolio_project_id, skill_id, sort_order) VALUES
  ('00000000-0000-0000-0000-000000000401', '00000000-0000-0000-0000-000000000321', 0)
ON CONFLICT (portfolio_project_id, skill_id) DO NOTHING;

INSERT INTO publication (id, publication_key, content_status, publication_stage, year, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000501', 'qa-publication-bilingual', 'PUBLISHED', 'PUBLISHED', 2026, 0, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO publication_translation (id, publication_id, language_code, title, slug, abstract_text, authors_display, venue_display, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000511', '00000000-0000-0000-0000-000000000501', 'en', 'QA Bilingual Publication', 'qa-publication-bilingual', 'Neutral English QA publication abstract.', 'QA Author', 'QA Venue', 'QA Bilingual Publication', 'Neutral English QA publication.', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000512', '00000000-0000-0000-0000-000000000501', 'fa', 'انتشار دوزبانه آزمایشی', 'qa-publication-bilingual-fa', 'چکیده خنثی انتشار آزمایشی فارسی.', 'نویسنده آزمایشی', 'نشریه آزمایشی', 'انتشار دوزبانه آزمایشی', 'چکیده خنثی انتشار آزمایشی فارسی.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO publication (id, publication_key, content_status, publication_stage, year, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000502', 'qa-publication-en-only', 'PUBLISHED', 'PUBLISHED', 2026, 1, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO publication_translation (id, publication_id, language_code, title, slug, abstract_text, authors_display, venue_display, seo_title, seo_description, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000521', '00000000-0000-0000-0000-000000000502', 'en', 'QA English-only Publication', 'qa-publication-en-only', 'Neutral English-only QA publication abstract.', 'QA Author', 'QA Venue', 'QA English-only Publication', 'Neutral English-only QA publication.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO resume_entry (id, entry_type, status, started_on, ended_on, is_current, sort_order, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000601', 'RESEARCH', 'PUBLISHED', '2025-01-01', NULL, true, 0, now(), now(), 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO resume_entry_translation (id, resume_entry_id, language_code, title, organization, location, summary, created_at, updated_at, version) VALUES
  ('00000000-0000-0000-0000-000000000611', '00000000-0000-0000-0000-000000000601', 'en', 'QA Research Entry', 'QA Organization', 'Local', 'Neutral local QA resume entry.', now(), now(), 0),
  ('00000000-0000-0000-0000-000000000612', '00000000-0000-0000-0000-000000000601', 'fa', 'سابقه پژوهشی آزمایشی', 'سازمان آزمایشی', 'محلی', 'سابقه خنثی برای کنترل کیفی محلی.', now(), now(), 0)
ON CONFLICT (id) DO NOTHING;
