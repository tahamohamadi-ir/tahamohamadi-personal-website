// @vitest-environment node
import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'

const workingDirectory = process.cwd()
const projectRoot = path.basename(workingDirectory).toLowerCase() === 'frontend'
  ? path.dirname(workingDirectory)
  : workingDirectory

function readProjectFile(projectRelativePath) {
  const filePath = path.resolve(projectRoot, projectRelativePath)

  if (!existsSync(filePath)) {
    throw new Error(`NOT_IMPLEMENTED:${projectRelativePath}`)
  }

  return readFileSync(filePath, 'utf8')
}

describe('production-like QA deployment contract', () => {
  it('keeps the edge, SSR origin, and database inside the explicit QA profile', () => {
    const compose = readProjectFile('compose.yaml')

    expect(compose).toMatch(/backend:/)
    expect(compose).toMatch(/frontend:/)
    expect(compose).toMatch(/nginx:/)
    expect(compose).toMatch(/profiles:\s*\[qa\]/)
    expect(compose).toMatch(/TAHA_BACKEND_ORIGIN:\s*http:\/\/backend:8080/)
    expect(compose).toMatch(/TAHA_PUBLIC_PORT/)
    expect(compose).toMatch(/taha_qa_postgres_data/)
  })

  it('preserves /api paths and forwards public host metadata at the Nginx edge', () => {
    const nginx = readProjectFile('infra/nginx/default.conf')

    expect(nginx).toMatch(/location \^~ \/api\//)
    expect(nginx).toMatch(/proxy_pass http:\/\/backend:8080;/)
    expect(nginx).toMatch(/proxy_set_header Host \$http_host;/)
    expect(nginx).toMatch(/proxy_set_header X-Forwarded-Host \$http_host;/)
    expect(nginx).toMatch(/proxy_set_header X-Forwarded-Proto \$scheme;/)
    expect(nginx).toMatch(/proxy_pass http:\/\/frontend:3000;/)
  })

  it('sets an explicit edge envelope above the backend multipart request limit', () => {
    const nginx = readProjectFile('infra/nginx/default.conf')
    const application = readProjectFile('backend/src/main/resources/application.yml')

    expect(application).toContain('max-file-size: 20971520B')
    expect(application).toContain('max-request-size: 22020096B')
    expect(nginx).toMatch(/client_max_body_size\s+22m;/)
  })

  it('uses an IPv4 loopback health probe for the Nginx container', () => {
    const compose = readProjectFile('compose.yaml')

    expect(compose).toMatch(/http:\/\/127\.0\.0\.1:8080\/healthz/)
  })

  it('loads deterministic QA data only through an explicit Spring profile', () => {
    const qaProfile = readProjectFile('backend/src/main/resources/application-qa.yml')
    const qaSeed = readProjectFile('backend/src/main/resources/qa-data.sql')

    expect(qaProfile).toMatch(/mode:\s*always/)
    expect(qaProfile).toMatch(/classpath:qa-data\.sql/)
    expect(qaSeed).toContain('qa-blog-bilingual')
    expect(qaSeed).toContain('qa-missing-translation')
    expect(qaSeed).toContain('qa-publication-en-only')
    expect(qaSeed).toMatch(
      /'00000000-0000-0000-0000-000000000521'[\s\S]*?'qa-publication-en-only'[\s\S]*?\nON CONFLICT \(id\) DO UPDATE SET created_at = EXCLUDED\.created_at, updated_at = EXCLUDED\.updated_at;/
    )
  })

  it('creates a lockfile for Quasar generated SSR runtime metadata before its clean install', () => {
    const dockerfile = readProjectFile('frontend/Dockerfile')

    expect(dockerfile).toMatch(
      /npm install --package-lock-only --ignore-scripts --no-audit --no-fund/
    )
    expect(dockerfile).toMatch(
      /npm ci --omit=dev --ignore-scripts --no-audit --no-fund/
    )
  })

  it('serves sitemap.xml from the internal public sitemap-data contract', () => {
    const server = readProjectFile('frontend/src-ssr/server.js')

    expect(server).toMatch(/app\.get\('\/sitemap\.xml'/)
    expect(server).toContain('TAHA_BACKEND_ORIGIN')
    expect(server).toContain('/api/v1/public/sitemap-data')
    expect(server).toMatch(/application\/xml/)
  })

  it('uses Quasar\'s store entrypoint so SSR route snapshots hydrate before mount', () => {
    const quasarConfig = readProjectFile('frontend/quasar.config.js')
    const storeEntry = readProjectFile('frontend/src/stores/index.js')

    expect(quasarConfig).not.toMatch(/boot:\s*\[[^\]]*'pinia'/)
    expect(storeEntry).toMatch(/createPinia/)
    expect(storeEntry).toMatch(/export default/)
  })
})
