#!/bin/bash
set -euo pipefail

# === 필수 환경변수 ===
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
ECR_REGISTRY="${ECR_REGISTRY:?ERROR: ECR_REGISTRY must be set}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
GITHUB_REPO="${GITHUB_REPO:?ERROR: GITHUB_REPO must be set}"
COMMIT_SHA="${COMMIT_SHA:-main}"

APP_DIR="/home/ubuntu/app"
mkdir -p "$APP_DIR/nginx/conf.d"
mkdir -p "$APP_DIR/nginx/certbot/conf" "$APP_DIR/nginx/certbot/www"
cd "$APP_DIR"

echo "=== Ditda 배포 ==="
echo "Image: ${ECR_REGISTRY}:${IMAGE_TAG}"

# === 최신 설정 파일 fetch (GitHub raw) ===
echo "[0/6] 설정 파일 가져오기"
RAW_BASE="https://raw.githubusercontent.com/${GITHUB_REPO}/${COMMIT_SHA}"

curl -fsSL "${RAW_BASE}/docker-compose.prod.yaml" -o docker-compose.prod.yaml
curl -fsSL "${RAW_BASE}/nginx/nginx.conf" -o nginx/nginx.conf
curl -fsSL "${RAW_BASE}/nginx/conf.d/nginx-blue.conf.template" -o nginx/conf.d/nginx-blue.conf.template
curl -fsSL "${RAW_BASE}/nginx/conf.d/nginx-green.conf.template" -o nginx/conf.d/nginx-green.conf.template

# === ECR 로그인 ===
echo "[1/6] ECR 로그인"
REGISTRY_HOST="${ECR_REGISTRY%%/*}"
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${REGISTRY_HOST}"

# === 새 이미지 pull ===
echo "[2/6] 이미지 pull"
docker pull "${ECR_REGISTRY}:${IMAGE_TAG}"

# === Parameter Store에서 환경변수 fetch ===
echo "[3/6] 환경변수 로드"
> .env
chmod 600 .env

cat >> .env << EOF
SPRING_PROFILES_ACTIVE=prod
ECR_REGISTRY=${ECR_REGISTRY}
IMAGE_TAG=${IMAGE_TAG}
EOF

aws ssm get-parameters-by-path \
  --path "/ditda/prod/" \
  --with-decryption \
  --region "${AWS_REGION}" \
  --query "Parameters[*].[Name,Value]" \
  --output text \
  | while IFS=$'\t' read -r name value; do
      key="${name##*/}"
      echo "${key}=${value}" >> .env
    done

PARAM_COUNT=$(grep -c '=' .env || true)
echo "    로드된 환경변수: ${PARAM_COUNT}개"

# === 필수 환경변수 검증 ===
REQUIRED_VARS=(
  # DB
  "DB_URL"
  "DB_USERNAME"
  "DB_PASSWORD"
  # Redis
  "REDIS_HOST"
  "REDIS_PORT"
  "REDIS_PASSWORD"
  # RabbitMQ
  "RABBITMQ_HOST"
  "RABBITMQ_PORT"
  "RABBITMQ_USERNAME"
  "RABBITMQ_PASSWORD"
  # JWT
  "JWT_SECRET_KEY"
  "JWT_ACCESS_TOKEN_EXPIRATION"
  "JWT_REFRESH_TOKEN_EXPIRATION"
  "JWT_REFRESH_TOKEN_HASH_SECRET"
  "JWT_COOKIE_SECURE"
  "JWT_REFRESH_COOKIE_PATH"
  # S3
  "S3_PUBLIC_BUCKET"
  "S3_PRIVATE_BUCKET"
  # Encrypt
  "ENCRYPT_SECRET_KEY"
  # Admin
  "ADMIN_NOTIFICATION_EMAIL"
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

# === 초기 배포 분기 ===
if [ ! -f nginx/conf.d/default.conf ]; then
  echo "[4/6] 초기 배포 — blue로 시작"
  cp nginx/conf.d/nginx-blue.conf.template nginx/conf.d/default.conf
  docker compose -f docker-compose.prod.yaml up -d redis nginx blue

  echo "[5/6] blue health check"
  for i in $(seq 1 60); do
    if curl -fs http://localhost:8081/actuator/health > /dev/null 2>&1; then
      echo "    [$i/60] health check 성공"
      docker compose -f docker-compose.prod.yaml up -d certbot
      echo "✅ 첫 배포 완료"
      exit 0
    fi
    echo "    [$i/60] 대기중..."
    sleep 3
  done

  echo "❌ blue 부팅 실패"
  docker logs ditda-blue --tail 50
  exit 1
fi

# === 디스크 정리 ===
docker image prune -f > /dev/null

# === 현재 active 판단 ===
ACTIVE=$(grep -oE 'server (blue|green):8080;' nginx/conf.d/default.conf \
  | head -n 1 | grep -oE 'blue|green')

if [ "$ACTIVE" = "blue" ]; then
  NEW="green"
  PORT=8082
else
  NEW="blue"
  PORT=8081
fi

echo "[4/6] 현재 active: $ACTIVE → 새 배포: $NEW"

# === 새 컨테이너 기동 ===
docker compose -f docker-compose.prod.yaml up -d "$NEW"

# === Health Check ===
echo "[5/6]  Health Check"
HEALTH_OK=false
for i in $(seq 1 60); do
  if curl -fs "http://localhost:${PORT}/actuator/health" > /dev/null 2>&1; then
    echo "    [$i/60]  Health Check 성공"
    HEALTH_OK=true
    break
  fi
  echo "    [$i/60] 대기중..."
  sleep 3
done

if [ "$HEALTH_OK" = false ]; then
  echo "❌  Health Check 실패. 컨테이너 로그:"
  docker logs "ditda-${NEW}" --tail 50
  docker compose -f docker-compose.prod.yaml stop "$NEW"
  exit 1
fi

# === Nginx 템플릿 교체 ===
echo "[6/6] 트래픽 전환 ($ACTIVE → $NEW)"
cp "nginx/conf.d/nginx-${NEW}.conf.template" "nginx/conf.d/default.conf"

if ! docker exec ditda-nginx nginx -t 2>/dev/null; then
  echo "❌ Nginx 설정 검증 실패. 롤백"
  cp "nginx/conf.d/nginx-${ACTIVE}.conf.template" "nginx/conf.d/default.conf"
  docker compose -f docker-compose.prod.yaml stop "$NEW"
  exit 1
fi

if ! docker exec ditda-nginx nginx -s reload; then
  echo "❌ Nginx reload 실패. 롤백"
  cp "nginx/conf.d/nginx-${ACTIVE}.conf.template" "nginx/conf.d/default.conf"
  docker exec ditda-nginx nginx -s reload
  docker compose -f docker-compose.prod.yaml stop "$NEW"
  exit 1
fi

# === 이전 컨테이너 종료 ===
sleep 5
docker compose -f docker-compose.prod.yaml stop "$ACTIVE"

docker system prune -f > /dev/null

echo "✅ 배포 완료 ($ACTIVE → $NEW)"