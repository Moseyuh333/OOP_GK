#!/usr/bin/env bash
# Build + chạy toàn bộ test SmartSportHub.
# Driver SQLite tải tự động nếu chưa có (không commit jar 14MB vào git).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

JAR="lib/sqlite-jdbc-3.46.1.3.jar"
JAR_URL="https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.3/sqlite-jdbc-3.46.1.3.jar"

if [ ! -f "$JAR" ]; then
  echo "==> Tải driver SQLite (${JAR})"
  mkdir -p lib
  curl -fsSL -o "$JAR" "$JAR_URL"
fi

CP="$JAR"

echo "==> Biên dịch mã nguồn"
rm -rf out
javac -Xlint:all -encoding UTF-8 -cp "$CP" -d out $(find src -name "*.java")

echo "==> Biên dịch kiểm thử"
rm -rf test-out
javac -encoding UTF-8 -cp "out;$CP" -d test-out $(find test -name "*.java")

echo "==> Chạy kiểm thử"
status=0
for suite in FieldDomainTest BookingDomainTest SeedDataValidationTest JsonContractTest \
             ApiServerTest DatabaseTest BookingServiceTest WriteApiTest; do
  if ! java -Dfile.encoding=UTF-8 -cp "out;test-out;$CP" "smartsporthub.test.$suite" | tail -1; then
    status=1
  fi
done

if [ "$status" -ne 0 ]; then
  echo "==> CÓ BỘ TEST KHÔNG ĐẠT" >&2
  exit 1
fi
echo "==> Tất cả kiểm thử đạt"