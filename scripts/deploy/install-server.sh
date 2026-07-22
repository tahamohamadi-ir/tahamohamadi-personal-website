#!/usr/bin/env bash

set -Eeuo pipefail
umask 027

APP_DIR="/opt/taha/repository"
STATE_DIR="/opt/taha/deploy"

CADDYFILE="/etc/caddy/Caddyfile"
CADDY_VERSION_DIR="/var/lib/caddy/taha-deployment"
CADDY_VERSION_FILE="${CADDY_VERSION_DIR}/version.txt"

DEPLOY_SOURCE="/tmp/taha-deploy"
WATCH_SOURCE="/tmp/taha-deploy-watch"

DEPLOY_TARGET="/usr/local/sbin/taha-deploy"
WATCH_TARGET="/usr/local/sbin/taha-deploy-watch"

SERVICE_FILE="/etc/systemd/system/taha-deploy-watch.service"
TIMER_FILE="/etc/systemd/system/taha-deploy-watch.timer"

for REQUIRED_PATH in \
    "$APP_DIR" \
    "$CADDYFILE" \
    "$DEPLOY_SOURCE" \
    "$WATCH_SOURCE"; do

    if [ ! -e "$REQUIRED_PATH" ]; then
        echo "Required installation path does not exist: $REQUIRED_PATH" >&2
        exit 1
    fi
done

echo "=== VALIDATE DEPLOYMENT SCRIPTS ==="

bash -n "$DEPLOY_SOURCE"
bash -n "$WATCH_SOURCE"

echo "Deployment script syntax: valid"

echo
echo "=== INSTALL DEPLOYMENT SCRIPTS ==="

install \
    -o root \
    -g root \
    -m 0750 \
    "$DEPLOY_SOURCE" \
    "$DEPLOY_TARGET"

install \
    -o root \
    -g root \
    -m 0750 \
    "$WATCH_SOURCE" \
    "$WATCH_TARGET"

echo
echo "=== PREPARE DEPLOYMENT STATE ==="

install \
    -d \
    -o root \
    -g root \
    -m 0700 \
    "$STATE_DIR"

CURRENT_SHA="$(
    runuser \
        -u deploy \
        -- \
        git \
            -C "$APP_DIR" \
            rev-parse \
            HEAD
)"

if [ ! -s "${STATE_DIR}/current.env" ]; then
    cat > "${STATE_DIR}/current.env" <<STATE
PROD_BACKEND_IMAGE=taha-prod-backend:latest
PROD_FRONTEND_IMAGE=taha-prod-frontend:latest
PROD_DEPLOYED_SHA=${CURRENT_SHA}
STATE

    chmod \
        0600 \
        "${STATE_DIR}/current.env"
fi

echo "Current Production SHA: $CURRENT_SHA"

echo
echo "=== PREPARE DEPLOYMENT VERSION ENDPOINT ==="

install \
    -d \
    -o caddy \
    -g caddy \
    -m 0755 \
    "$CADDY_VERSION_DIR"

TEMP_VERSION_FILE="$(mktemp)"
printf '%s\n' "$CURRENT_SHA" > "$TEMP_VERSION_FILE"

install \
    -o caddy \
    -g caddy \
    -m 0644 \
    "$TEMP_VERSION_FILE" \
    "$CADDY_VERSION_FILE"

rm -f "$TEMP_VERSION_FILE"

CADDY_BACKUP="${CADDYFILE}.before-deployment-version-$(date -u +%Y%m%dT%H%M%SZ)"

cp \
    "$CADDYFILE" \
    "$CADDY_BACKUP"

chmod \
    0600 \
    "$CADDY_BACKUP"

if ! grep \
    -Fq \
    '@deploymentVersion path /deployment-version' \
    "$CADDYFILE"; then

    python3 \
        - "$CADDYFILE" <<'PY'
from pathlib import Path
import sys

path = Path(sys.argv[1])
content = path.read_text(encoding="utf-8")

marker = "(taha_application_routes) {\n"

handler = """(taha_application_routes) {
\t@deploymentVersion path /deployment-version

\thandle @deploymentVersion {
\t\theader Cache-Control "no-store, no-cache, must-revalidate"
\t\troot * /var/lib/caddy/taha-deployment
\t\trewrite * /version.txt
\t\tfile_server
\t}

"""

if marker not in content:
    raise SystemExit(
        "Could not locate the taha_application_routes snippet."
    )

path.write_text(
    content.replace(
        marker,
        handler,
        1
    ),
    encoding="utf-8"
)
PY
fi

caddy fmt \
    --overwrite \
    "$CADDYFILE"

if ! caddy validate \
    --config "$CADDYFILE" \
    --adapter caddyfile; then

    cp \
        "$CADDY_BACKUP" \
        "$CADDYFILE"

    caddy fmt \
        --overwrite \
        "$CADDYFILE" \
        >/dev/null \
        2>&1 \
        || true

    systemctl reload \
        caddy \
        || true

    echo "Caddy validation failed; previous configuration restored." >&2
    exit 1
fi

if ! systemctl reload caddy; then
    cp \
        "$CADDY_BACKUP" \
        "$CADDYFILE"

    caddy fmt \
        --overwrite \
        "$CADDYFILE" \
        >/dev/null \
        2>&1 \
        || true

    systemctl reload \
        caddy \
        || true

    echo "Caddy reload failed; previous configuration restored." >&2
    exit 1
fi

DEPLOYMENT_VERSION="$(
    curl \
        --fail \
        --silent \
        --show-error \
        --resolve "tahamohamadi.ir:443:127.0.0.1" \
        https://tahamohamadi.ir/deployment-version \
    | tr \
        -d \
        '[:space:]'
)"

if [ "$DEPLOYMENT_VERSION" != "$CURRENT_SHA" ]; then
    echo "Deployment version endpoint returned an unexpected SHA." >&2
    exit 1
fi

echo "Deployment version endpoint: valid"

echo
echo "=== INSTALL SYSTEMD SERVICE ==="

cat > "$SERVICE_FILE" <<'UNIT'
[Unit]
Description=Taha Production deployment watcher
Requires=docker.service
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
User=root
Group=root
ExecStart=/usr/local/sbin/taha-deploy-watch
TimeoutStartSec=25m
Nice=10
IOSchedulingClass=best-effort
IOSchedulingPriority=7
UNIT

cat > "$TIMER_FILE" <<'UNIT'
[Unit]
Description=Check for approved Taha Production deployments

[Timer]
OnCalendar=*-*-* *:*:00
Persistent=true
RandomizedDelaySec=15s
AccuracySec=10s
Unit=taha-deploy-watch.service

[Install]
WantedBy=timers.target
UNIT

chmod \
    0644 \
    "$SERVICE_FILE" \
    "$TIMER_FILE"

systemctl daemon-reload

systemctl enable \
    --now \
    taha-deploy-watch.timer

systemctl reset-failed \
    taha-deploy-watch.service \
    2>/dev/null \
    || true

echo
echo "=== VERIFY INSTALLATION ==="

systemctl is-enabled \
    taha-deploy-watch.timer

systemctl is-active \
    taha-deploy-watch.timer

systemctl list-timers \
    taha-deploy-watch.timer \
    --all \
    --no-pager

stat \
    -c 'Owner=%U Group=%G Mode=%a Path=%n' \
    "$DEPLOY_TARGET" \
    "$WATCH_TARGET" \
    "$STATE_DIR" \
    "$CADDY_VERSION_FILE" \
    "$SERVICE_FILE" \
    "$TIMER_FILE"

echo
echo "=== RUN INITIAL WATCH CHECK ==="

systemctl start \
    taha-deploy-watch.service

systemctl status \
    taha-deploy-watch.service \
    --no-pager \
    --lines=80 \
    || true

rm -f \
    "$DEPLOY_SOURCE" \
    "$WATCH_SOURCE"

echo
echo "PULL-BASED PRODUCTION CD INSTALLED"
