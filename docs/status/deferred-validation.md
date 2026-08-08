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

- **وضعیت:** `OPEN`
- **شدت:** `P3`
- **تاریخ ثبت:** `2026-08-08`
- **Scope / مرجع:** graphify-out پس از تغییر T0.3 Page Revision
- **آنچه انجام شد:** `graphify update .` اجرا شد؛ AST برای 168 فایل بازاستخراج شد، اما ابزار از overwrite خودداری کرد چون graph جدید 4245 node و graph فعلی 4246 node داشت. همچنین 3 node مربوط به 2 فایل خارج‌شده از corpus را fail-closed نگه داشت و نبود parser SQL را گزارش کرد.
- **آنچه عمداً انجام نشد:** full re-extraction، نصب dependency اختیاری SQL parser یا اجرای `--force` بدون بازبینی علت اختلاف انجام نشد.
- **اثر و ریسک:** runtime یا قرارداد محصول متاثر نیست؛ graph برای navigation ممکن است کمی stale بماند.
- **Mitigation فعلی:** در این slice مسیر controller، service و testها مستقیماً از source بررسی شد و graph موجود overwrite نشد.
- **مالک:** owner tooling/repository maintenance
- **Trigger بازگشت:** پیش از task بعدی که به graph traversal متکی است یا در maintenance دوره‌ای graph
- **معیار بستن:** اختلاف node و تغییر ignore rules بررسی شود، سپس full/incremental update سالم با manifest سازگار اجرا و evidence ثبت شود.
- **شواهد رفع:** —
