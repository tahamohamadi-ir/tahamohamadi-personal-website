# پلن اجرایی Gap-based برای CMS و Public Experience 2.0

**وضعیت:** پیشنهادی برای اجرا  
**مبنای برنامه:** وضعیت فعلی مخزن در 2026-07-25، سند `tahamohamadi_site_cms_v2_development_plan.md` و تصمیم incremental V2  
**راهبرد تحویل:** Vertical releases قابل‌آزمون و قابل rollback  
**معماری مرجع:** Composer رابطه‌ای typed، DTOهای موجود، SSR عمومی، PostgreSQL/Flyway و optimistic locking

## هدف

تکمیل تدریجی CMS و تجربه عمومی نسل دوم، بدون بازنویسی Composer فعلی یا شکستن قراردادهای پذیرفته‌شده. هر release باید به‌تنهایی قابل استفاده، تست و بازگردانی باشد.

## قیود سراسری

- پیش از هر تغییر backend یا Admin، محدودیت رسمی M1 باید با یک charter پس از M1 به‌روزرسانی و تأیید شود.
- تغییرات dirty موجود نباید overwrite شوند؛ ابتدا باید مالکیت، ارتباط با Release 1 و وضعیت تستشان مشخص شود.
- فارسی و انگلیسی مستقل‌اند؛ fallback محتوایی میان localeها ممنوع است.
- schema فقط از طریق Flyway و قراردادهای API فقط با DTO تغییر می‌کنند.
- APIهای Admin باید authentication، authorization، CSRF، audit log و optimistic locking را حفظ کنند.
- SSR عمومی فقط محتوای منتشرشده را render می‌کند. Preview هرگز indexable یا cacheable نیست.
- هر release قبل از شروع release بعدی، تست‌های مرتبط، SSR build، بررسی RTL/LTR و `git diff --check` را می‌گذراند.

## قراردادها و مرزهای معماری

| حوزه | قرارداد اجرایی |
|---|---|
| Page Composition | Composer رابطه‌ای typed منبع حقیقت باقی می‌ماند؛ JSONB جایگزین document-wide برای صفحه نیست. |
| Public API | تغییرات additive هستند؛ route و payload پذیرفته‌شده بدون دلیل migration حذف یا تغییر ناسازگار نمی‌کنند. |
| Admin API | endpointهای جدید فقط زیر مرز Admin و با DTOهای validate‌شده ایجاد می‌شوند. |
| Preview | داده Draft را با token کوتاه‌عمر و no-store نمایش می‌دهد؛ public response را تغییر نمی‌دهد. |
| i18n | content، slug، SEO metadata، alt و caption به‌صورت مستقل per locale مدیریت می‌شوند. |
| Rich content | HTML خام، script و URL ناامن fail-closed هستند؛ output قبل از یک sink کنترل‌شده sanitize می‌شود. |
| Media | انتخاب UI با asset object انجام می‌شود؛ Raw Asset ID در تجربه کاربر نمایش داده نمی‌شود. |

---

## Release 0 — Governance، baseline و تکمیل Home 2.0

### R0.1 — تثبیت مبنای پروژه

- [ ] charter پس از M1 را ایجاد و scope مجاز V2، feature flagها، policy migration، rollback و مالک releaseها را ثبت کنید.
- [ ] تغییرات dirty موجود را به یکی از حالت‌های `task-owned`، `pre-existing` یا `discard candidate` طبقه‌بندی کنید؛ هیچ فایل pre-existing را بدون تأیید تغییر ندهید.
- [ ] inventory routeهای Public/Admin، DTOها، migrationهای V1 تا V10، block typeها، lifecycleها، media flow و تست‌های موجود را تولید کنید.
- [ ] هر نیاز V2 را در matrix با وضعیت `exists`، `partial`، `missing` یا `deferred` ثبت کنید.

**خروجی:** matrix قابل‌ردیابی و charter تأییدشده.  
**گیت:** هیچ تغییر schema/API پیش از تأیید charter شروع نمی‌شود.

### R0.2 — Baseline کیفیت

- [ ] از `/fa` و `/en` در breakpointهای 375، 768، 1024 و 1440px screenshot تهیه کنید.
- [ ] baseline SSR، hydration، heading hierarchy، focus order، overflow، reduced motion، loading، empty و error state را ثبت کنید.
- [ ] browser matrix و accessibility baseline را برای desktop و mobile مشخص کنید.
- [ ] baseline performance شامل LCP، CLS، حجم SSR payload و chunkهای ابتدایی را ثبت کنید.

**خروجی:** artefactهای baseline و معیار مقایسهٔ releaseها.

### R0.3 — تکمیل Home 2.0 روی Composer موجود

- [ ] renderer عمومی را برای blockهای مجاز Home تکمیل کنید: hero، research focus، selected work، featured publications، latest writing و contact CTA.
- [ ] collection خالی را حذف کنید؛ placeholder، metric ساختگی و محتوای فرضی ممنوع است.
- [ ] CTA را به مسیرهای locale-aware داخلی یا HTTPS محدود کنید.
- [ ] detail linkهای collection را از canonical path API و فقط در همان locale تولید کنید.
- [ ] hero media را فقط با alt تأییدشدهٔ همان locale نشان دهید.
- [ ] دقیقاً یک H1 در صفحهٔ Home حفظ کنید؛ `main`، `lang` و `dir` فقط مالکیت layout عمومی هستند.

### R0.4 — گیت محتوای Home

- [ ] checklist دو‌زبانه برای portrait/hero، selected work، publication و Open Graph asset بسازید.
- [ ] کیفیت و وضعیت انتشار هر content item را پیش از اتصال به Home تأیید کنید.
- [ ] در نبود asset یا translation لازم، section را حذف یا release را hold کنید.

### R0.5 — تست و پذیرش Home

- [ ] تست SSR، hydration، یک H1، مسیر CTA امن، alt هم‌زبان، عدم fallback، collection link و block ناشناخته را اضافه کنید.
- [ ] بررسی دستی RTL/LTR، mobile، focus، 44px target، contrast و layout shift را انجام دهید.
- [ ] build SSR و full unit suite را اجرا کنید؛ وضعیت هر blocker محیطی را جداگانه ثبت کنید.

**معیار پذیرش Release 0:** Home با دادهٔ واقعی CMS render می‌شود؛ `/fa` و `/en` مستقل و هم‌سطح‌اند؛ محتوای ناقص یا placeholder در production دیده نمی‌شود.

---

## Release 1 — Media Picker و Media Library قابل‌اعتماد

### R1.1 — قرارداد و gap analysis رسانه

- [ ] endpointهای media، pagination، asset status، metadata، upload policy، orphan report و archive behavior موجود را مستند کنید.
- [ ] محدودیت extension، MIME، size، نام‌گذاری، storage path، public URL و lifecycle را با policy امنیتی تطبیق دهید.
- [ ] هر تغییر لازم را به migration، DTO، validation و test مستقل بشکنید؛ تغییر ناسازگار در response موجود انجام ندهید.

### R1.2 — Media Picker مشترک

- [ ] picker مشترک با preview، search، type filter، pagination، select، clear، replace و upload-in-flow بسازید.
- [ ] فیلدهای مصرف‌کننده فقط asset معتبر و type مجاز همان فیلد را بپذیرند.
- [ ] loading، empty، upload progress، validation error، CSRF/authorization failure و recoverable retry را نمایش دهید.
- [ ] Site Settings، Open Graph، page block، blog و portfolio را به picker مشترک منتقل کنید.

### R1.3 — عملیات Library

- [ ] grid/list، search/filter/sort، metadata detail و translation metadata را اضافه کنید.
- [ ] alt و caption فارسی/انگلیسی را مستقل و با status قابل‌مشاهده مدیریت کنید.
- [ ] usage detail را برای Composer، blog، portfolio و settings نمایش دهید.
- [ ] archive یا replace برای asset استفاده‌شده باید مصرف‌کنندگان و اثر تغییر را قبل از mutation نشان دهد.

### R1.4 — Usage و lifecycle رسانه

- [ ] extraction reference را برای همهٔ domainهای mediaدار کامل کنید.
- [ ] archive را reversible و public rendering asset archive‌شده را ناممکن کنید.
- [ ] orphan report را با filterهای قابل‌فهم و pagination قابل‌کران ارائه کنید.

### R1.5 — spike variants و focal point

- [ ] نیاز variant، checksum، focal point، bulk action و ظرفیت storage را با دادهٔ واقعی ارزیابی کنید.
- [ ] برای هر قابلیت تأییدشده ADR، budget storage، migration plan و rollback note ثبت کنید.

**تست‌های لازم:** MIME/type/size، unauthorized mutation، pagination bounds، upload failure، picker integration، asset in use، archive conflict، alt مستقل locale و عدم نشت internal path.

**معیار پذیرش Release 1:** تمام انتخاب‌های media از یک flow قابل‌جست‌وجو انجام می‌شوند و هیچ Raw Asset ID در UI نیست.

---

## Release 2 — Composer افزایشی، Canvas و Preview

### R2.1 — Section aggregate و migration سازگار

- [ ] مدل Section افزایشی متصل به صفحه و blockهای typed را طراحی کنید.
- [ ] ordering، enabled state، shared layout، version و رفتار حذف/restore را تعیین کنید.
- [ ] migration باید page و block موجود را بدون تغییر خروجی عمومی به ساختار جدید منتقل کند.
- [ ] migration rehearsal و rollback را با dataset مشابه production اجرا کنید.

### R2.2 — Validation و projection سمت سرور

- [ ] validator برای block type مجاز، settings whitelist، ordering، media reference و completeness locale بسازید.
- [ ] payload ناشناخته یا block نامعتبر را با Problem Details رد کنید.
- [ ] projection مشترک public/preview را ایجاد کنید تا renderer دو منبع داده نداشته باشد.
- [ ] block ناشناخته در public fail-closed حذف و در admin به‌صورت diagnostic گزارش شود.

### R2.3 — Canvas و Inspector Admin

- [ ] Composer فعلی را به canvas با section library، block library، inspector و validation summary توسعه دهید.
- [ ] add، delete، duplicate و reorder را با actionهای واضح پیاده‌سازی کنید.
- [ ] drag-and-drop باید معادل keyboard move-up/move-down و focus-preserving داشته باشد.
- [ ] dirty guard، save state و 409 conflict dialog را روی تمام mutationها اعمال کنید.

### R2.4 — Device و locale preview

- [ ] preview محافظت‌شده برای desktop/tablet/mobile و هر locale بسازید.
- [ ] draft preview باید noindex، no-store و غیرقابل‌دسترسی بدون token باشد.
- [ ] preview و public از همان block registry و renderer استفاده کنند.

### R2.5 — Undo/redo و autosave محدود

- [ ] command stack محلی برای undo/redo بسازید؛ پس از save موفق reset شود.
- [ ] autosave فقط برای Draft، با debounce، saving/saved/error state و recovery marker فعال باشد.
- [ ] autosave در conflict، offline یا validation failure نباید overwrite یا publish انجام دهد.

**تست‌های لازم:** migration، validation، public/preview parity، SSR، unknown block، keyboard reorder، device/locale preview، stale version، autosave offline و recovery.

**معیار پذیرش Release 2:** مدیر می‌تواند Home را با blockهای مجاز بسازد؛ ترتیب بعد از refresh حفظ می‌شود؛ preview با public برابر است و keyboard مسیر کامل را پوشش می‌دهد.

---

## Release 3 — Article Editor و Blog Experience

### R3.1 — Technical Spike و ADR editor

- [ ] گزینه‌های editor را حداکثر طی سه روز با Vue 3، SSR، RTL، accessibility، JSON document model، custom node، paste cleanup، bundle size، license، maintenance و testability امتیازدهی کنید.
- [ ] adapter داخلی، تصمیم vendor، fallback و dependency budget را در ADR ثبت کنید.
- [ ] تا تأیید ADR، dependency جدید یا migration editor وارد شاخهٔ اجرا نشود.

### R3.2 — مدل سند و سازگاری Markdown

- [ ] editor را پشت adapter داخلی نگه دارید تا domain به vendor وابسته نشود.
- [ ] Markdown فعلی باید import شود؛ export برای blockهای قابل‌تبدیل تضمین و syntax غیرقابل‌تبدیل با warning روشن گزارش شود.
- [ ] سند versioned هر translation را افزایشی اضافه کنید؛ markdown قدیمی تا پایان migration/audit قابل‌خواندن بماند.
- [ ] HTML خام، script و URL ناامن ذخیره یا رندر نشود.

### R3.3 — MVP block catalogue و authoring

- [ ] paragraph، heading، list، image، gallery، caption، quote، code، divider، callout و reference را پیاده‌سازی کنید.
- [ ] slash command، keyboard shortcut، paste cleanup، inline media picker، caption/alt و preview را اضافه کنید.
- [ ] video/embed/download فقط بعد از allow-list و URL validation وارد scope شوند.

### R3.4 — Blog عمومی

- [ ] renderer امن، TOC، reading time، print CSS، related content و previous/next را پیاده‌سازی کنید.
- [ ] Blog landing شامل featured/latest/topics/archive، pagination و filter URL-state باشد.
- [ ] structured data فقط برای مقالهٔ published و complete تولید شود.

**تست‌های لازم:** Markdown migration، sanitizer/XSS، unsafe URL، SSR/hydration، inline media، RTL code/quote، TOC، print، draft exclusion و editor accessibility.

**معیار پذیرش Release 3:** نویسنده می‌تواند مقاله دو‌زبانه با media و blockهای پایه بسازد و reader آن را بدون layout shift و با SSR امن بخواند.

---

## Release 4 — Portfolio Case Study و تکمیل اتصال Home

### R4.1 — تکمیل مدل Portfolio

- [ ] فیلدهای موجود را با role، team، client، date range، technologies، outcome، status و relationهای skill/publication مقایسه کنید.
- [ ] فقط فیلدهای غایب را با migration، DTO، validation، index و test افزایشی اضافه کنید.
- [ ] metadata قابل‌نمایش عمومی باید locale-aware، قابل‌اعتبارسنجی و بدون data fabrication باشد.

### R4.2 — Case Study composition

- [ ] narrative دو‌زبانه، gallery، facts، outcome، related content و CTA را با Composer موجود مدل کنید.
- [ ] layout و content را جدا نگه دارید و usage رسانه را ثبت کنید.
- [ ] public detail را با semantic facts، gallery و SEO metadata تکمیل کنید.

### R4.3 — Portfolio landing و Home integration

- [ ] landing با filter، pagination و featured state ارائه کنید.
- [ ] selected work، research focus، publication و writing در Home فقط از دادهٔ published و complete خوانده شوند.
- [ ] motion محدود، قابل‌کاهش و بدون اثر منفی بر focus یا Core Web Vitals باشد.

**معیار پذیرش Release 4:** هر project منتشرشده Case Study قابل‌فهم و locale-complete دارد؛ Home فقط دادهٔ واقعی و آمادهٔ انتشار نشان می‌دهد.

---

## Release 5 — Workflow، Revision و Translation Freshness

### R5.1 — Lifecycle state machine

- [ ] lifecycle فعلی را برای Draft، In Review، Scheduled، Published و Archived توسعه دهید.
- [ ] برای هر transition، permission، validation، audit event، optimistic version و Problem Details مشخص کنید.
- [ ] mutation نامعتبر یا بدون نقش مجاز باید با تست منفی پوشش داده شود.

### R5.2 — Revision و restore

- [ ] snapshot immutable برای page، article و case study ایجاد کنید.
- [ ] list، compare و restore باید audit داشته باشند و version conflict را کنترل کنند.
- [ ] restore محتوای live را مستقیم overwrite نکند؛ نسخه بازیابی‌شده ابتدا Draft باشد.

### R5.3 — Scheduling و Preview Token

- [ ] schedule timezone-aware، job idempotent، retry policy، failure log و cancel flow را بسازید.
- [ ] preview token کوتاه‌عمر، محدود به content/locale و قابل‌ابطال باشد.
- [ ] token نباید در log، referrer، analytics یا public cache ظاهر شود.

### R5.4 — Translation workflow

- [ ] وضعیت‌های Missing، Incomplete، Complete و Outdated را از قواعد قابل‌تست استخراج کنید.
- [ ] تغییر source locale باید مقصد را Outdated کند، نه اینکه متن آن را overwrite کند.
- [ ] queue شامل filter، source update time، side-by-side compare و completion checklist باشد.

### R5.5 — سطوح Admin workflow

- [ ] revision timeline، compare/restore، scheduled queue، activity log، conflict dialog و translation queue را به dashboard و entity pageها اضافه کنید.

**تست‌های لازم:** transition غیرمجاز، timezone، retry/idempotency، restore، 409، token expiry/revocation، public exclusion و Outdated logic.

**معیار پذیرش Release 5:** محتوای زمان‌بندی‌شده بدون دخالت دستی منتشر می‌شود، failure قابل‌مشاهده است و revision قبلی به‌طور امن بازیابی می‌شود.

---

## Release 6 — SEO، Accessibility، Performance، Security و عملیات

### R6.1 — SEO Audit

- [ ] completeness metadata، canonical/hreflang، Open Graph، broken internal link، missing alt و structured-data validity را بررسی کنید.
- [ ] یافته‌ها را فقط برای routeهای public و published در Admin گزارش دهید.
- [ ] sitemap و robots نباید admin، preview، draft، archive یا media خصوصی را expose کنند.

### R6.2 — Accessibility و Visual Regression

- [ ] keyboard flow، visible focus، dialog semantics، heading order، contrast، 44px targets، screen-reader smoke و reduced-motion را پوشش دهید.
- [ ] fixtureهای فارسی/انگلیسی و screenshot regression برای صفحه‌های بحرانی ایجاد کنید.

### R6.3 — Performance

- [ ] image dimension/format policy، lazy loading non-critical، route splitting، bundle analysis و SSR payload budget تعریف کنید.
- [ ] LCP، CLS و public route budget را در CI قابل‌سنجش کنید.

### R6.4 — Security و Observability

- [ ] CSP سازگار با SSR/media، security headerها، upload defense، rate limit مسیرهای حساس و log redaction را بازبینی کنید.
- [ ] health/readiness، scheduler/media metric، structured error logging و dashboard ownership را تکمیل کنید.
- [ ] backup/restore، migration، rollback، incident و capacity runbook را ثبت و rehearsal کنید.

**معیار پذیرش Release 6:** critical SEO و accessibility finding صفر است، بودجهٔ performance برقرار است و rollback/runbook قابل‌اجرا هستند.

---

## Release 7 — Rollout و Stabilization

- [ ] feature flag مستقل برای Home 2.0، Composer sections، article editor، case study و workflow اضافه کنید.
- [ ] backup/restore، migration، dual-read لازم، rollback و data audit را در staging تمرین کنید.
- [ ] browser matrix، mobile/desktop، real content، permission matrix، media storage، schedule، SSR SEO و performance را sign-off کنید.
- [ ] deployment order، post-deploy smoke، alert threshold، owner و rollback owner را در release runbook ثبت کنید.

**معیار پذیرش Release 7:** rollout مرحله‌ای قابل rollback است و production handoff شامل monitoring، ownership و checklist محتوایی کامل است.

## ماتریس تست حداقلی

- **Backend:** service unit، repository/API integration، Flyway migration، protected endpoint، validation و public published-only behavior.
- **Frontend:** component/store/renderer، media picker، form validation، locale status، conflict، preview و SSR build.
- **E2E:** login → media select → draft compose → locale preview → publish؛ article import/edit/publish؛ schedule/retry/restore؛ anonymous visitor فقط published content را می‌بیند.
- **دستی:** `/fa` و `/en` در چهار breakpoint، keyboard-only، screen reader smoke، no-placeholder، no-cross-locale fallback و screenshot comparison با محتوای واقعی.

## Definition of Done برای هر release

- [ ] API/DTO، migration، validation و audit impact مشخص است.
- [ ] حالت‌های loading، empty، error، offline و conflict پوشش داده شده‌اند.
- [ ] رفتار فارسی RTL و انگلیسی LTR مستقل تست شده است.
- [ ] تست unit/integration/component/E2E متناسب با ریسک سبز است.
- [ ] SSR build، accessibility و performance gate مرتبط عبور کرده‌اند.
- [ ] rollback note، documentation و release evidence ثبت شده است.
- [ ] هیچ secret، token، password یا credential در repository، browser storage یا log ثبت نشده است.
