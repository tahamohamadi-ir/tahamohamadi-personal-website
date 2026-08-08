package ir.tahamohamadi.demo;

import ir.tahamohamadi.common.domain.LanguageCode;
import ir.tahamohamadi.content.page.ContentPage;
import ir.tahamohamadi.content.page.ContentPageRepository;
import ir.tahamohamadi.content.page.ContentPageTranslation;
import ir.tahamohamadi.content.page.ContentPageTranslationRepository;
import ir.tahamohamadi.media.asset.MediaAsset;
import ir.tahamohamadi.media.asset.MediaAssetRepository;
import ir.tahamohamadi.media.asset.MediaAssetTranslation;
import ir.tahamohamadi.media.asset.MediaAssetTranslationRepository;
import ir.tahamohamadi.media.storage.MediaStorage;
import ir.tahamohamadi.portfolio.project.PortfolioProject;
import ir.tahamohamadi.portfolio.project.PortfolioProjectMedia;
import ir.tahamohamadi.portfolio.project.PortfolioProjectMediaRepository;
import ir.tahamohamadi.portfolio.project.PortfolioProjectRepository;
import ir.tahamohamadi.portfolio.project.PortfolioProjectTranslation;
import ir.tahamohamadi.portfolio.project.PortfolioProjectTranslationRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Intentional development-only data. It never runs without both the demo
 * profile and an explicit property, and it refuses to merge into an existing
 * CMS so genuine editorial data cannot be overwritten.
 */
@Component
@Profile("demo")
@ConditionalOnProperty(name = "taha.demo-seed.enabled", havingValue = "true")
class DemoContentSeeder implements ApplicationRunner {
    private final ContentPageRepository pages;
    private final ContentPageTranslationRepository pageTranslations;
    private final MediaAssetRepository media;
    private final MediaAssetTranslationRepository mediaTranslations;
    private final PortfolioProjectRepository projects;
    private final PortfolioProjectTranslationRepository projectTranslations;
    private final PortfolioProjectMediaRepository projectMedia;
    private final MediaStorage storage;
    private final JdbcTemplate jdbc;

    DemoContentSeeder(
            ContentPageRepository pages,
            ContentPageTranslationRepository pageTranslations,
            MediaAssetRepository media,
            MediaAssetTranslationRepository mediaTranslations,
            PortfolioProjectRepository projects,
            PortfolioProjectTranslationRepository projectTranslations,
            PortfolioProjectMediaRepository projectMedia,
            MediaStorage storage,
            JdbcTemplate jdbc
    ) {
        this.pages = pages;
        this.pageTranslations = pageTranslations;
        this.media = media;
        this.mediaTranslations = mediaTranslations;
        this.projects = projects;
        this.projectTranslations = projectTranslations;
        this.projectMedia = projectMedia;
        this.storage = storage;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments arguments) {
        if (hasActiveCmsData()) {
            return;
        }

        Instant now = Instant.now();
        MediaAsset hero = media("demo/hero-workspace.png", "hero-workspace.png", now);
        MediaAsset board = media("demo/case-study-board.png", "case-study-board.png", now);
        saveLocalizedMediaMetadata(hero, "فضای کاری پژوهش و طراحی سیستم", "Research and systems-design workspace", now);
        saveLocalizedMediaMetadata(board, "برد فرایند پژوهش و طراحی", "Research and design process board", now);

        seedSiteIdentity(now);
        seedHome(hero, board, now);
        seedPortfolioProject(hero, board, now);
    }

    private boolean hasActiveCmsData() {
        return hasActiveRows("content_page")
                || hasActiveRows("portfolio_project")
                || hasActiveRows("site_setting")
                || hasActiveRows("media_asset");
    }

    private boolean hasActiveRows(String table) {
        return jdbc.queryForObject("select count(*) from " + table + " where deleted_at is null", Long.class) > 0;
    }

    private MediaAsset media(String storageKey, String filename, Instant now) {
        MediaAsset existing = media.findByStorageKeyAndDeletedAtIsNull(storageKey).orElse(null);
        if (existing != null) return existing;
        ClassPathResource source = new ClassPathResource("demo-media/" + filename);
        try (InputStream input = source.getInputStream()) {
            storage.store(storageKey, input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store demo media " + filename, exception);
        }
        try (InputStream input = source.getInputStream()) {
            return media.saveAndFlush(MediaAsset.create(
                    UUID.randomUUID(), storageKey, filename, "png", "image/png", source.contentLength(), sha256(input), 1536, 1536, now
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to checksum demo media " + filename, exception);
        }
    }

    private void saveLocalizedMediaMetadata(MediaAsset asset, String faAlt, String enAlt, Instant now) {
        saveMediaTranslation(asset, LanguageCode.fa, faAlt, now);
        saveMediaTranslation(asset, LanguageCode.en, enAlt, now);
    }

    private void saveMediaTranslation(MediaAsset asset, LanguageCode locale, String alt, Instant now) {
        if (mediaTranslations.findByMediaAssetIdAndLanguageCodeAndDeletedAtIsNull(asset.getId(), locale).isEmpty()) {
            mediaTranslations.save(MediaAssetTranslation.create(UUID.randomUUID(), asset, locale, alt, null, now));
        }
    }

    private void seedSiteIdentity(Instant now) {
        if (jdbc.queryForObject("select count(*) from site_setting where deleted_at is null", Long.class) > 0) return;
        UUID id = UUID.randomUUID();
        jdbc.update("insert into site_setting (id,brand_name,brand_tagline,theme_preset,layout_density,created_at,updated_at,version) values (?,?,?,?,?,?,?,0)",
                id, "Taha Mohamadi", "Human-centered systems", "EDITORIAL_NAVY", "COMFORTABLE", now, now);
        siteIdentityTranslation(id, "fa", "طه محمدی", "سامانه‌های انسان‌محور", now);
        siteIdentityTranslation(id, "en", "Taha Mohamadi", "Human-centered systems", now);
    }

    private void siteIdentityTranslation(UUID settingId, String locale, String name, String tagline, Instant now) {
        jdbc.update("insert into site_setting_translation (id,site_setting_id,language_code,brand_name,brand_tagline,created_at,updated_at,version) values (?,?,?,?,?,?,?,0)",
                UUID.randomUUID(), settingId, locale, name, tagline, now, now);
    }

    private void seedHome(MediaAsset hero, MediaAsset board, Instant now) {
        ContentPage home = ContentPage.create(UUID.randomUUID(), "home", now);
        home.publish(now);
        pages.saveAndFlush(home);
        pageTranslations.saveAllAndFlush(List.of(
                pageTranslation(home, LanguageCode.fa, "خانه", "home", "طراحی و ساخت سامانه‌هایی که به تجربهٔ انسان احترام می‌گذارند.", now),
                pageTranslation(home, LanguageCode.en, "Home", "home", "Designing systems that respect human experience.", now)
        ));

        UUID sectionId = UUID.randomUUID();
        jdbc.update("insert into content_page_section (id,content_page_id,section_type,layout,sort_order,is_enabled,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,0)",
                sectionId, home.getId(), "STANDARD", "SINGLE_COLUMN", 0, true, now, now);
        insertBlock(sectionId, home.getId(), 0, "HERO", hero.getId(),
                "طراحی سامانه‌های انسان‌محور", "Human-centered systems design",
                "از پژوهش تا اجرای محصول، با تصمیم‌هایی روشن و قابل ارزیابی.", "From research to delivery, with clear and testable decisions.",
                "فضای کاری پژوهش و طراحی سیستم", "Research and systems-design workspace", now);
        insertBlock(sectionId, home.getId(), 1, "MEDIA_TEXT", board.getId(),
                "فرایند، نه نمایش", "Process, not theatre",
                "نمونهٔ توسعه‌ای برای نمایش مسیر پژوهش، طراحی و بازبینی در CMS.", "Development fixture showing the research, design, and review path in the CMS.",
                "برد فرایند پژوهش و طراحی", "Research and design process board", now);
    }

    private ContentPageTranslation pageTranslation(ContentPage page, LanguageCode locale, String title, String slug, String summary, Instant now) {
        ContentPageTranslation translation = ContentPageTranslation.create(UUID.randomUUID(), page, locale, title, slug, now);
        translation.update(title, slug, summary, null, title, summary, null);
        return translation;
    }

    private void insertBlock(UUID sectionId, UUID pageId, int order, String type, UUID mediaId, String faTitle, String enTitle, String faLead, String enLead, String faAlt, String enAlt, Instant now) {
        UUID blockId = UUID.randomUUID();
        String settings = "{\"mediaId\":\"" + mediaId + "\",\"decorative\":false}";
        jdbc.update("insert into content_page_block (id,content_page_id,content_page_section_id,block_type,sort_order,is_enabled,settings_json,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,0)",
                blockId, pageId, sectionId, type, order, true, settings, now, now);
        blockTranslation(blockId, "fa", faTitle, faLead, faAlt, now);
        blockTranslation(blockId, "en", enTitle, enLead, enAlt, now);
    }

    private void blockTranslation(UUID blockId, String locale, String title, String lead, String alt, Instant now) {
        jdbc.update("insert into content_page_block_translation (id,content_page_block_id,language_code,title,eyebrow,lead,body_markdown,action_label,action_path,alt_text,created_at,updated_at,version) values (?,?,?,?,?,?,?,?,?,?,?,?,0)",
                UUID.randomUUID(), blockId, locale, title, null, lead, null, null, null, alt, now, now);
    }

    private void seedPortfolioProject(MediaAsset cover, MediaAsset gallery, Instant now) {
        PortfolioProject project = PortfolioProject.create(UUID.randomUUID(), "research-systems-demo", cover, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1), null, null, 0, now);
        project.publish(now);
        projects.saveAndFlush(project);
        PortfolioProjectTranslation fa = PortfolioProjectTranslation.create(UUID.randomUUID(), project, LanguageCode.fa, "سامانهٔ پژوهش و تصمیم", "research-systems-demo", "نمونهٔ دوزبانهٔ توسعه‌ای برای نمایش ساختار نمونه‌کار.", "این محتوا فقط برای محیط توسعه است و از CMS می‌آید.", "سامانهٔ پژوهش و تصمیم", "نمونهٔ توسعه‌ای", now);
        fa.updateCaseStudyFacts("طراحی و پیاده‌سازی", "نمونهٔ توسعه", "یک نفر", "الگوی قابل‌ارزیابی");
        PortfolioProjectTranslation en = PortfolioProjectTranslation.create(UUID.randomUUID(), project, LanguageCode.en, "Research and decision system", "research-systems-demo", "A bilingual development fixture for the portfolio structure.", "This content is development-only and comes through the CMS.", "Research and decision system", "Development fixture", now);
        en.updateCaseStudyFacts("Design and implementation", "Development fixture", "One person", "An evaluable pattern");
        projectTranslations.saveAllAndFlush(List.of(fa, en));
        projectMedia.saveAndFlush(PortfolioProjectMedia.attach(project, gallery, 0));
    }

    private static String sha256(InputStream input) throws IOException {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input.readAllBytes()));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
