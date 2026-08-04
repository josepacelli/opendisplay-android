#!/usr/bin/env bash
# Builda o debug e instala em todo device já conectado (USB ou wifi já pareado/conectado).
# Pra conectar um device por wifi antes, ver scripts/install-wifi.sh.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

echo "Buildando debug..."
./gradlew assembleDebug

APK="$REPO_ROOT/app/build/outputs/apk/debug/app-debug.apk"

DEVICES="$(adb devices | awk 'NR>1 && $2=="device" {print $1}')"

if [[ -z "$DEVICES" ]]; then
  echo "Nenhum device conectado (adb devices vazio)." >&2
  echo "Por wifi: scripts/install-wifi.sh pair/install <ip:porta>" >&2
  exit 1
fi

while IFS= read -r device; do
  echo "Instalando em $device..."
  adb -s "$device" install -r "$APK"
done <<< "$DEVICES"
