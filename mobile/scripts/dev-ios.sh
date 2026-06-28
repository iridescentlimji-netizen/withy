#!/usr/bin/env bash
# Metro 시작 → 번들 준비 → 앱 실행 (까만 화면 방지용 원스톱)
set -euo pipefail

cd "$(dirname "$0")/.."
export REACT_NATIVE_PACKAGER_HOSTNAME=127.0.0.1

METRO_URL="http://127.0.0.1:8081"
BUNDLE_URL="${METRO_URL}/.expo/.virtual-metro-entry.bundle?platform=ios&dev=true"

pkill -f "expo start" 2>/dev/null || true
pkill -f "metro" 2>/dev/null || true
sleep 1

echo "▶ Metro 시작 (${METRO_URL})..."
npx expo start --localhost --clear &
METRO_PID=$!

cleanup() {
  kill "$METRO_PID" 2>/dev/null || true
}
trap cleanup EXIT INT TERM

echo "▶ 번들 준비 대기 (최대 90초)..."
for i in $(seq 1 90); do
  if curl -sf "${BUNDLE_URL}" >/dev/null; then
    echo "▶ Metro 준비 완료"
    break
  fi
  if [ "$i" -eq 90 ]; then
    echo "❌ Metro 번들 타임아웃"
    exit 1
  fi
  sleep 1
done

bash scripts/ios-open.sh

echo ""
echo "▶ Metro 유지 중 (종료: Ctrl+C)"
wait "$METRO_PID"
