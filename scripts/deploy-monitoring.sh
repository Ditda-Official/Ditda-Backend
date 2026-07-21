#!/bin/bash
set -euo pipefail

# === 필수 환경변수 ===
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
GITHUB_REPO="${GITHUB_REPO:?ERROR: GITHUB_REPO must be set}"
COMMIT_SHA="${COMMIT_SHA:-main}"

MON_DIR="/home/ubuntu/monitoring"
mkdir -p "$MON_DIR"
cd "$MON_DIR"

echo "=== Ditda 모니터링 서버 배포 ==="
echo "Commit: ${COMMIT_SHA}"

# === 설정 파일 가져오기 (monitoring/server 하위만 추출) ===
echo "[1/5] 설정 파일 가져오기"
TARBALL="$(mktemp)"
trap 'rm -f "$TARBALL"' EXIT
curl -fsSL "https://github.com/${GITHUB_REPO}/archive/${COMMIT_SHA}.tar.gz" -o "$TARBALL"
tar -xzf "$TARBALL" --wildcards --strip-components=3 \
  -C "$MON_DIR" "*/monitoring/server/*"

# === 시크릿 확인  ===
echo "[2/5] 시크릿 확인"
for f in nginx/.htpasswd nginx/certs/mon.crt nginx/certs/mon.key; do
  if [ ! -f "$f" ]; then
    echo "❌ ${MON_DIR}/${f} 없음 — 수동 생성이 필요합니다."
    exit 1
  fi
done

# === Parameter Store에서 환경변수 fetch ===
echo "[3/5] 환경변수 로드"
: > .env
chmod 600 .env

aws ssm get-parameters-by-path \
  --path "/ditda/monitoring/" \
  --with-decryption \
  --region "${AWS_REGION}" \
  --query "Parameters[*].[Name,Value]" \
  --output text \
  | while IFS=$'\t' read -r name value; do
      echo "${name##*/}=${value}" >> .env
    done

REQUIRED_VARS=(
  # S3
  "S3_LOKI_BUCKET"
  "S3_TEMPO_BUCKET"
  "S3_REGION"
  "S3_ENDPOINT"

  # Grafana
  "GRAFANA_ADMIN_USER"
  "GRAFANA_ADMIN_PASSWORD"
)

MISSING=()
for var in "${REQUIRED_VARS[@]}"; do
  if ! grep -qE "^${var}=.+" .env; then
    MISSING+=("$var")
  fi
done

if [ ${#MISSING[@]} -gt 0 ]; then
  echo "❌ 필수 환경변수 누락 또는 값 비어있음:"
  for var in "${MISSING[@]}"; do
    echo "    - ${var}"
  done
  exit 1
fi

echo "    필수 환경변수 ${#REQUIRED_VARS[@]}개 모두 존재 확인"

# === 새 컨테이너 기동 ===
echo "[4/5] 컨테이너 기동"
docker compose -f docker-compose.prod.yaml up -d --force-recreate --remove-orphans
docker image prune -f > /dev/null

# === Health Check ===
echo "[5/5] Health Check"
HEALTH_OK=false
for i in $(seq 1 40); do
  if curl -fs http://localhost:3000/api/health > /dev/null 2>&1; then
    echo "    [$i/40] Grafana 응답 확인"
    HEALTH_OK=true
    break
  fi
  echo "    [$i/40] 대기중..."
  sleep 3
done

if [ "$HEALTH_OK" = false ]; then
  echo "❌ Grafana Health Check 실패"
  docker compose -f docker-compose.prod.yaml logs --tail 50 grafana
  exit 1
fi

for c in ditda-prometheus ditda-loki ditda-tempo ditda-monitoring-proxy ditda-grafana; do
  STATE=$(docker inspect -f '{{.State.Status}}' "$c" 2>/dev/null || echo "missing")
  if [ "$STATE" != "running" ]; then
    echo "❌ ${c} 상태 이상: ${STATE}"
    docker logs "$c" --tail 50 2>/dev/null || true
    exit 1
  fi
  echo "    ${c}: running"
done

echo "✅ 모니터링 서버 배포 완료"