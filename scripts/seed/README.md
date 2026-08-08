# Seed محتوای نمونهٔ CMS

این seed فقط برای محیط توسعه است. با profile `demo` و property صریح فعال می‌شود، تنها روی CMS کاملاً خالی اجرا می‌شود و هیچ دادهٔ موجودی را تغییر نمی‌دهد یا با آن ترکیب نمی‌کند.

```powershell
cd backend
.\mvnw.cmd "-Dspring-boot.run.profiles=local,demo" "-Dspring-boot.run.arguments=--taha.demo-seed.enabled=true" spring-boot:run
```

در نخستین اجرای موفق، Home دوزبانه، دو block تصویر‌دار، تنظیمات هویت سایت، و یک نمونه‌کار دوزبانه همراه با gallery ساخته می‌شود. تصاویر زیر از `backend/src/main/resources/demo-media/` به storage فعال Media Library کپی و به‌صورت assetهای معمول CMS ثبت می‌شوند.

- `hero-workspace.png`
- `case-study-board.png`

این محتوا برای production نیست. برای جایگزینی آن از Admin/CMS استفاده کنید؛ هیچ جزء public به URL یا متن نمونهٔ ثابت وابسته نیست.
