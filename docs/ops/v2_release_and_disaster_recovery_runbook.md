# دفترچه راهنمای انتشار و بازیابی از بحران V2 (V2 Release & Disaster Recovery Runbook)

**پروژه:** TahaMohamadi.ir  
**نسخه:** 2.0  
**تاریخ:** 2026-07-27  

---

## 1. فاز پیش از انتشار (Pre-Deployment Checklist)

1. **اعتبارسنجی تست‌ها:**
   - فرانت‌اند: `cd frontend && npm run test:unit` (۴۰ فایل تست سبز).
   - بک‌اند: `cd backend && .\mvnw.cmd test` (۱۸ تست واحد سبز).
2. **بررسی پشتیبان‌گیری دیتابیس (PostgreSQL Backup):**
   ```bash
   docker exec -t tahamohamadi-db pg_dump -U postgres -d tahamohamadi_db -F c -b -v -f /var/backups/db_pre_v2_$(date +%Y%m%d_%H%M%S).dump
   ```
3. **پشتیبان‌گیری از فایل‌های رسانه (Media Storage Backup):**
   ```bash
   tar -czvf /var/backups/media_assets_$(date +%Y%m%d_%H%M%S).tar.gz /var/tahamohamadi/uploads
   ```

---

## 2. ترتیب اجرای انتشار در محیط تولید (Production Deployment Order)

1. **اعمال مایگریشن‌های Flyway دیتابیس:**
   مایگریشن‌های `V11` تا `V22` به صورت کاملاً ایزوله و افزایشی اجرا می‌شوند و به داده‌های نسخه ۱ آسیبی وارد نمی‌کنند.
2. **راه‌اندازی سرویس Backend (Spring Boot Container):**
   ```bash
   docker compose up -d --no-deps --build backend
   ```
3. **راه‌اندازی سرویس Frontend (Vue/Quasar SSR Container):**
   ```bash
   docker compose up -d --no-deps --build frontend
   ```
4. **تست سلامت نهایی (Post-Deploy Smoke Test):**
   - فراخوانی `/api/v1/public/features` برای اطمینان از سلامت سرویس Feature Flags.
   - فراخوانی `/fa` و `/en` برای اطمینان از رندرینگ کامل SSR و عدم وجود Layout Shift.

---

## 3. دستورالعمل بازگردانی و بازیابی از بحران (Rollback & Disaster Recovery)

در صورت بروز هرگونه مشکل بحرانی در محیط تولید:

1. **غیرفعال‌سازی ویژگی‌های V2 با Feature Flag:**
   با تنظیم متغیرهای محیطی زیر در Docker Compose بدون نیاز به Rollback دیتابیس، امکان سوییچ سریع وجود دارد:
   ```env
   TAHA_FEATURES_HOME_V2_ENABLED=false
   TAHA_FEATURES_COMPOSER_CANVAS_ENABLED=false
   ```
2. **بازگردانی نسخه دیتابیس:**
   ```bash
   docker exec -i tahamohamadi-db pg_restore -U postgres -d tahamohamadi_db --clean /var/backups/db_pre_v2_LATEST.dump
   ```
3. **بازگردانی فایل‌های رسانه:**
   ```bash
   tar -xzvf /var/backups/media_assets_LATEST.tar.gz -C /var/tahamohamadi/uploads
   ```
