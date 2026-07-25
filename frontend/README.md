# Frontend

Vue 3 + Quasar SSR frontend for the bilingual public site and its session-protected Admin CMS.

## Local public development

The SSR development server proxies same-origin `/api` calls to Spring Boot. Start the backend first, then launch the frontend with its origin explicitly configured.

```powershell
# Terminal 1 — from the repository root
$env:SPRING_PROFILES_ACTIVE = 'local'
.\backend\mvnw.cmd spring-boot:run

# Terminal 2 — from frontend/
$env:TAHA_BACKEND_ORIGIN = 'http://localhost:8080'
npm.cmd run dev -- --port 9100
```

If the browser shows the recoverable “Unable to load content” state at `http://localhost:9100`, verify that `http://localhost:8080/actuator/health/readiness` is reachable. The page is working as designed: it does not invent public content when the API is unavailable.

## Verification

```powershell
# from frontend/
npm.cmd run test:unit
npm.cmd run build
```

## Live Admin E2E

The Playwright suite intentionally targets the complete QA stack, not the Vite/Quasar development server. Start the stack from the repository root and wait until `nginx`, `frontend`, and `backend` are healthy before running the test.

```powershell
# repository root — credentials stay in the ignored QA environment file
docker compose --env-file .env.qa.local --profile qa up -d --build
docker compose --env-file .env.qa.local --profile qa ps

# frontend/ — map the provisioned ephemeral QA credential without printing it
$qa = @{}
Get-Content ..\.env.qa.local | ForEach-Object {
  if ($_ -match '^(TAHA_QA_ADMIN_EMAIL|TAHA_QA_ADMIN_PASSWORD)=(.*)$') {
    $qa[$matches[1]] = $matches[2]
  }
}
$env:TAHA_E2E_BASE_URL = 'http://localhost:8088'
$env:TAHA_E2E_ADMIN_EMAIL = $qa['TAHA_QA_ADMIN_EMAIL']
$env:TAHA_E2E_ADMIN_PASSWORD = $qa['TAHA_QA_ADMIN_PASSWORD']
$env:TAHA_E2E_BROWSER_EXECUTABLE = 'C:\Program Files\Google\Chrome\Application\chrome.exe'
$env:TAHA_E2E_DISABLE_VIDEO = 'true'
npm.cmd run test:e2e
```

`ERR_CONNECTION_REFUSED` for `localhost:8088` means the QA Nginx service was not running; it is not an Admin UI or authentication regression. Do not use production credentials for this suite.
