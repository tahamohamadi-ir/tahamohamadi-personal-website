# موارد معوق، ریسک‌ها و اعتبارسنجی‌های باقی‌مانده

این فایل ledger رسمی پروژه برای هر تست، QA، بررسی امنیتی، hardening یا ریسکِ آگاهانهٔ به‌تعویق‌افتاده است. سیاست کامل در [سیاست تحویل سریع](../governance/fast-track-delivery.md) قرار دارد.

## قواعد استفاده

- هر entry باید شناسه، تاریخ، scope، شدت، دلیل تعویق، اثر، mitigation، مالک، trigger بازگشت و evidence رفع داشته باشد.
- وضعیت‌ها فقط `OPEN`، `ACCEPTED_TEMPORARILY`، `BLOCKED` و `RESOLVED` هستند.
- `P0` و `P1` برای scope درگیر، با ثبت‌شدن قابل انتشار نمی‌شوند؛ ledger فقط ریسک را آشکار می‌کند.
- با رفع مورد، entry را حذف نکنید؛ وضعیت را `RESOLVED` و شواهد را ثبت کنید.
- تعویق‌های مخزن یا stack دیگر به این فایل منتقل نمی‌شوند مگر دوباره در همین مخزن اثبات و اولویت‌بندی شده باشند.

## قالب entry

```md
### ID — عنوان کوتاه

- **وضعیت:** `OPEN`
- **شدت:** `P2`
- **تاریخ ثبت:** `YYYY-MM-DD`
- **Scope / مرجع:** task، requirement، فایل یا release مرتبط
- **آنچه انجام شد:** شواهد test/QA/inspection موجود
- **آنچه عمداً انجام نشد:** بررسی یا hardening معوق
- **اثر و ریسک:** اثر واقعی بر کاربر، داده یا عملیات
- **Mitigation فعلی:** محدودسازی، feature flag، runbook یا نبود mitigation
- **مالک:** نام/role مسئول
- **Trigger بازگشت:** milestone، قبل از release مشخص، یا رخداد قابل‌سنجش
- **معیار بستن:** test/QA/مدرک لازم برای `RESOLVED`
- **شواهد رفع:** فقط هنگام بستن تکمیل شود
```

## موارد باز

### CMS-DEMO-SEED-010 — اجرای واقعی محتوای نمونهٔ توسعه‌ای

- **وضعیت:** `OPEN`
- **شدت:** `P2`
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** `DemoContentSeeder`، `scripts/seed/README.md` و تکمیل اجرایی R1 در [plan 012](../../plans/012-cms-v2-wordpress-capability-task-list.md)
- **آنچه انجام شد:** seed به دو guard مستقل (`demo` profile و `taha.demo-seed.enabled=true`) محدود است، فقط CMS کاملاً خالی را می‌پذیرد و Home/Portfolio دوزبانه و دو asset معمولی Media Library را از resourceهای توسعه می‌سازد. backend همراه با sourceهای جدید compile و `MediaValidationUnitTest` با 5 test سبز شد.
- **آنچه عمداً انجام نشد:** اجرای seed روی PostgreSQL توسعه، مشاهدهٔ SSR مسیرهای `/fa` و `/en` و صفحهٔ نمونه‌کار، و پاکسازی دادهٔ حاصل از اجرای آزمایشی هنوز انجام نشده است.
- **اثر و ریسک:** تا زمان اجرای واقعی، ناسازگاری JDBC/فایل‌سیستم یا mapping public فقط در محیط عملی آشکار می‌شود؛ دادهٔ نمونه نباید production-ready تلقی شود.
- **Mitigation فعلی:** seed با profile و property صریح، guard CMS خالی و resourceهای داخلی محدود شده و در profile پیش‌فرض فعال نیست.
- **مالک:** owner توسعهٔ CMS
- **Trigger بازگشت:** پیش از استفادهٔ demo برای بازبینی محصول، QA بصری یا هر محیط غیرمحلی
- **معیار بستن:** یک اجرای موفق روی PostgreSQL توسعه، ثبت نتیجهٔ Home و نمونه‌کار در هر دو locale و حذف/بازنشانی دادهٔ آزمایشی طبق runbook.
- **شواهد رفع:** —

### CMS-R1-VALIDATION-005 — اجرای تازهٔ تست‌ها و QA Release 1 Media

- **وضعیت:** `OPEN`
- **شدت:** `P1` برای اعلام عملیاتی‌بودن Release 1
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T1.1، T1.2 و T1.4](../../plans/012-cms-v2-wordpress-capability-task-list.md)
- **آنچه انجام شد:** قراردادهای upload/list/select/replace/archive به‌صورت ایستا ممیزی شد؛ testهای منفی type field، انتخاب چندگانهٔ picker و backend type guardهای Portfolio/Publication/Resume افزوده شدند. دو Vitest متمرکز با مجموع 15 test و `MediaValidationUnitTest` با 5 test عبور کردند؛ backend و همهٔ test sourceها نیز compile شدند.
- **آنچه عمداً انجام نشد:** `AdminProjectIntegrationTest`، `PublicationResumeIntegrationTest` و `MediaUploadIntegrationTest` تلاش شدند اما پیش از test body به‌دلیل نبود Docker/Testcontainers اجرا نشدند؛ E2E «upload → select → save» و QA session معتبر Admin نیز باقی است.
- **اثر و ریسک:** تغییرهای selector یا validation ممکن است در integration runtime، CSRF/multipart یا flow واقعی فرم‌ها شکست بخورند؛ Release 1 release-verified نیست.
- **Mitigation فعلی:** policy server برای محتوای upload و type guardهای domain فعال است؛ تغییرهای frontend کوچک و shared هستند.
- **مالک:** owner اجرای Release 1 CMS
- **Trigger بازگشت:** پیش از rollout Media Picker جدید یا اعلام قابلیت Media Library عملیاتی
- **معیار بستن:** integration testهای backend در محیط Docker سالم عبور کنند و یک flow واقعی upload، multiple select، save، replace و archive با session/CSRF معتبر ثبت شود.
- **شواهد رفع:** —

### CMS-R1-ALT-006 — اجرای integration policy alt تصاویر public

- **وضعیت:** `OPEN`
- **شدت:** `P1` برای انتشار public imageهای جدید
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T1.3](../../plans/012-cms-v2-wordpress-capability-task-list.md)
- **آنچه انجام شد:** Page Builder برای `HERO`، `MEDIA` و `MEDIA_TEXT` alt فارسی/انگلیسی یا `decorative=true` را الزام می‌کند و renderer meaningful media را fail-closed نگه می‌دارد. Portfolio gallery به‌صورت policy معنادار تعریف شد: الصاق و publish بدون هر دو alt در backend رد می‌شود و public API تنها alt همان locale را می‌دهد. `CollectionMedia` هم بدون alt محلی render نمی‌شود؛ logo سایت نام برند CMS را دریافت می‌کند.
- **آنچه عمداً انجام نشد:** اجرای HTTP integration برای رد gallery بدون alt و پاسخ fa/en، و QA دستی با asset واقعی، به‌دلیل نبود Docker/Testcontainers انجام نشده است.
- **اثر و ریسک:** قانون و renderer در source/test متمرکز پوشش دارند، اما binding runtime PostgreSQL و DOM واقعی هنوز evidence عملیاتی ندارند.
- **Mitigation فعلی:** تمام rendererهای فعلی Media Library fail-closed یا با alternative متن صریح‌اند؛ Page Builder decorative decision دارد و Portfolio gallery decorative را نمی‌پذیرد.
- **مالک:** owner مدل محتوای CMS
- **Trigger بازگشت:** پیش از rollout Media Library یا افزودن asset واقعی به landing/public page.
- **معیار بستن:** integration testهای backend در Docker برای reject/allow gallery و پاسخ fa/en، به‌همراه QA واقعی public images، اجرا و ثبت شوند.
- **شواهد رفع:** —

### CMS-R1-ORPHAN-007 — اعتبارسنجی HTTP گزارش orphan صفحه‌بندی‌شده

- **وضعیت:** `OPEN`
- **شدت:** `P2`
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T1.4](../../plans/012-cms-v2-wordpress-capability-task-list.md)؛ `MediaOrphanReportService`
- **آنچه انجام شد:** endpoint اکنون پس از refresh usage index، با `PageResponse` و filterهای query/type/status، orphanها را از کل library query می‌کند؛ پنل مستقل Admin list/pagination/empty state دارد و `MediaOrphanReportServiceUnitTest` اجرا و سبز شد.
- **آنچه عمداً انجام نشد:** اجرای HTTP/SQL واقعی این query و flow archive پس از حذف آخرین reference به‌دلیل نبود Docker/Testcontainers انجام نشده است.
- **اثر و ریسک:** syntax یا pagination query native ممکن است فقط در PostgreSQL runtime آشکار شود؛ تا آن زمان cleanup بزرگ release-verified نیست.
- **Mitigation فعلی:** محدودیت 100 مورد حذف شده، query بر usage index تازه‌شده تکیه دارد، archive server همچنان asset referenced را رد می‌کند.
- **مالک:** owner Media Library
- **Trigger بازگشت:** پیش از cleanup عملیاتی گسترده یا rollout Media Library.
- **معیار بستن:** integration PostgreSQL برای page/filter و حذف آخرین reference، به‌همراه یک QA session Admin، اجرا و سبز شود.
- **شواهد رفع:** —

### CMS-R1-PUBLICATION-COVER-008 — backend publication cover هنوز image-only نیست

- **وضعیت:** `OPEN`
- **شدت:** `P1` برای integrity محتوای Publication
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T1.2](../../plans/012-cms-v2-wordpress-capability-task-list.md)؛ `AdminPublicationService`
- **آنچه انجام شد:** picker فیلد cover در Admin Publications با `allowedTypes=['image']` محدود است و `AdminPublicationService` اکنون asset فعال غیرتصویری را در create/update رد می‌کند. test منفی integration افزوده و backend testCompile با موفقیت اجرا شد.
- **آنچه عمداً انجام نشد:** اجرای integration test واقعی به‌دلیل نبود Docker/Testcontainers انجام نشده است.
- **اثر و ریسک:** منطق server اکنون guard دارد، اما evidence HTTP/runtime آن تا اجرای Testcontainers کامل نیست.
- **Mitigation فعلی:** UI shared type policy، status active و guard نهایی service اعمال می‌شوند.
- **مالک:** owner Publication domain
- **Trigger بازگشت:** پیش از rollout Publications با cover یا هر API-client جدید
- **معیار بستن:** integration test مثبت/منفی create/update در Docker/Testcontainers اجرا شود.
- **شواهد رفع:** —

### CMS-R0-REVISION-001 — ناسازگاری مسیر restore در Page Edit

- **وضعیت:** `OPEN`
- **شدت:** `P1` برای قابلیت بازگردانی revision صفحه
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T0.3](../../plans/012-cms-v2-wordpress-capability-task-list.md)؛ `frontend/src/pages/admin/AdminPageEditPage.vue`
- **آنچه انجام شد:** بررسی ایستا ناسازگاری را نشان داد و UI به `restore-as-draft` با `version` فعلی اصلاح شد. دکمهٔ confirm در حین درخواست غیرفعال است و پس از پاسخ موفق، route به Draft تازه تغییر می‌کند تا mutation بعدی به source page برنگردد. test قراردادی frontend برای مسیر/پارامتر/409/redirect و test integration backend برای مسیر قدیمی، conflict نسخهٔ کهنه و مسیر صحیح اضافه شده‌اند.
- **آنچه عمداً انجام نشد:** اجرای Vitest و integration test تازه هنوز به‌عنوان evidence این slice ثبت نشده‌اند.
- **اثر و ریسک:** بازگردانی revision از Page Edit ممکن است شکست بخورد یا رفتار مورد انتظار را نشان ندهد؛ نباید این flow را release-verified اعلام کرد.
- **Mitigation فعلی:** تا زمان رفع، restore از Page Edit قابلیت قابل‌اتکا محسوب نشود.
- **مالک:** owner اجرای Release 0 CMS
- **Trigger بازگشت:** پیش از اعلام Page Revisions به‌عنوان قابلیت عملیاتی
- **معیار بستن:** درخواست UI به `restore-as-draft` با `version` صحیح برسد، 409 به‌درستی نمایش داده شود و test مثبت/منفی T0.3 عبور کند.
- **شواهد رفع:** —

### CMS-R0-BASELINE-002 — اعتبارسنجی‌های VERIFY در baseline CMS V2

- **وضعیت:** `OPEN`
- **شدت:** `P2`
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T0.1، T0.2 و T0.4](../../plans/012-cms-v2-wordpress-capability-task-list.md)
- **آنچه انجام شد:** وجود قابلیت‌های Media Picker، Media Library، Composer، Preview Token، workflow، Translation Queue و feature flagها با inspection کد در مرجع و task list ثبت شده است.
- **آنچه عمداً انجام نشد:** snapshot واقعی قراردادها، audit کامل route/flag و QA تازهٔ رفتارهای موجود هنوز به‌عنوان evidence این release ثبت نشده‌اند.
- **اثر و ریسک:** قابلیت‌های موجود ممکن است صرفاً «در کد حاضر» باشند و برای release آماده نباشند؛ بدون این evidence نباید به آن‌ها اتکا شود.
- **Mitigation فعلی:** قابلیت‌های دارای برچسب `VERIFY` فقط پس از verification متمرکز هر slice وارد scope عملیاتی شوند.
- **مالک:** owner اجرای Release 0 CMS
- **Trigger بازگشت:** قبل از شروع هر task وابسته در Release 1 تا 5
- **معیار بستن:** checklistهای T0.1، T0.2 و T0.4 با evidence آزمون/QA و config/rollback تکمیل شوند.
- **شواهد رفع:** —

### CMS-R0-PAGE-EDIT-002 — قراردادهای mutation در Page Edit

- **وضعیت:** `OPEN`
- **شدت:** `P1` برای ذخیره و lifecycle صفحه در Page Edit
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T0.4](../../plans/012-cms-v2-wordpress-capability-task-list.md)؛ `frontend/src/pages/admin/AdminPageEditPage.vue`
- **آنچه انجام شد:** inspection قرارداد نشان داد `PUT /admin/pages/{id}` به `version` در body و publish/archive به `version` در query نیاز دارند. UI برای هر دو اصلاح شد و test قراردادی frontend پوشش آن را اضافه کرد.
- **آنچه عمداً انجام نشد:** اجرای Vitest و integration/browser verification تازه هنوز ثبت نشده‌اند.
- **اثر و ریسک:** بدون `version`، save یا تغییر status از Page Edit با conflict شکست می‌خورد؛ تا اجرای verification، این flow release-verified نیست.
- **Mitigation فعلی:** مسیرهای Page Edit اکنون با contract controller هم‌راستا هستند؛ conflict موجود به‌صورت صریح در banner نشان داده می‌شود.
- **مالک:** owner اجرای Release 0 CMS
- **Trigger بازگشت:** پیش از اعلام Page Edit به‌عنوان قابلیت عملیاتی
- **معیار بستن:** test قراردادی frontend و integration testهای Page Audit/Concurrency عبور کنند و یک QA session واقعی save/publish/archive را با CSRF معتبر تأیید کند.
- **شواهد رفع:** —

### CMS-R0-FLAGS-004 — feature flagها هنوز rollout gate نیستند

- **وضعیت:** `OPEN`
- **شدت:** `P2`
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** [plan 012، T0.4](../../plans/012-cms-v2-wordpress-capability-task-list.md)؛ `FeatureFlagService` و `/api/v1/public/features`
- **آنچه انجام شد:** پنج flag و binding محیطی آن‌ها بررسی و در `.env.example` و `docs/ops/feature-flags.md` مستند شدند. test واحد موجود، default و override محدود service را پوشش می‌دهد.
- **آنچه عمداً انجام نشد:** هیچ controller، SSR renderer، navigation یا Admin workflow هنوز on/off این flagها را مصرف نمی‌کند؛ test رفتار واقعی flag=false/true نیز وجود ندارد.
- **اثر و ریسک:** تغییر environment variable اکنون rollback یا kill switch واقعی نیست و نباید برای release به آن اتکا شود.
- **Mitigation فعلی:** runbook صریحاً این محدودیت را اعلام می‌کند؛ rollback قابلیت‌های فعلی با artifact/deployment انجام می‌شود، نه feature flag.
- **مالک:** owner اجرای Release 0 CMS
- **Trigger بازگشت:** قبل از rollout نخستین قابلیت با هر یک از این flagها
- **معیار بستن:** هر flag مصرف‌شده backend و frontend/public امن، test on/off و rollback owner مشخص داشته باشد.
- **شواهد رفع:** —

### DEVEX-GRAPH-003 — به‌روزرسانی fail-closed knowledge graph

- **وضعیت:** `RESOLVED`
- **شدت:** `P3`
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** graphify-out پس از تغییر T0.3 Page Revision
- **آنچه انجام شد:** اجرای بعدی `graphify update .` با موفقیت graph را به 4252 node و 8954 edge بازسازی و `graph.json`، `graph.html` و `GRAPH_REPORT.md` را به‌روز کرد.
- **آنچه عمداً انجام نشد:** parser اختیاری SQL نصب نشد؛ warning آن اثری بر nodeهای Java/Vue این slice ندارد.
- **اثر و ریسک:** runtime یا قرارداد محصول متاثر نیست؛ graph فعلی برای navigation هم‌راستاست.
- **Mitigation فعلی:** graph به‌روز شد؛ warning SQL در maintenance جداگانه بررسی می‌شود.
- **مالک:** owner tooling/repository maintenance
- **Trigger بازگشت:** در صورت تغییر دوبارهٔ ignore rules یا نیاز به semantic SQL graph
- **معیار بستن:** اختلاف node و تغییر ignore rules بررسی شود، سپس full/incremental update سالم با manifest سازگار اجرا و evidence ثبت شود.
- **شواهد رفع:** `graphify update .` در 2026-08-08 با exit code 0 و بازسازی 4252 node / 8954 edge.
