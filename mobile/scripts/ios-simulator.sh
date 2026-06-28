#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

export REACT_NATIVE_PACKAGER_HOSTNAME=localhost

echo "▶ Metro + iOS 빌드/설치 (한 프로세스)..."
echo "   이미 설치됐고 Metro만 필요하면: npm run start:simulator → i 키"
echo ""

# Metro를 Expo가 먼저 띄운 뒤 앱을 설치합니다 (분리 실행 시 까만 화면 발생)
exec npx expo run:ios --port 8081
