#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
export REACT_NATIVE_PACKAGER_HOSTNAME=127.0.0.1

METRO_URL="http://127.0.0.1:8081"
BUNDLE_URL="${METRO_URL}/.expo/.virtual-metro-entry.bundle?platform=ios&dev=true"

if ! curl -sf "${METRO_URL}/status" >/dev/null; then
  echo "❌ Metro가 실행 중이 아닙니다. → npm run dev:ios"
  exit 1
fi

if ! curl -sf "${BUNDLE_URL}" >/dev/null; then
  echo "❌ Metro 번들을 받을 수 없습니다."
  exit 1
fi

open -a Simulator
xcrun simctl terminate booted host.exp.Exponent 2>/dev/null || true
xcrun simctl terminate booted com.anonymous.kid-schedule 2>/dev/null || true
sleep 1
xcrun simctl launch booted com.anonymous.kid-schedule

echo "✅ 「아이 스케줄」 앱 실행됨"
echo "   까맣으면 시뮬레이터 클릭 후 Cmd+R"
