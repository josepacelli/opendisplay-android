#!/usr/bin/env bash
# Instala o APK do OpenDisplay num device Android via ADB por wifi
# (depuração sem fio). Requer `adb` no PATH (brew install android-platform-tools).
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

usage() {
  cat <<EOF
Uso:
  $(basename "$0") pair <ip:porta_pareamento> <codigo>
      Pareia com o device (uma vez só; o pareamento fica salvo até "esquecer" o PC no device).
      IP:porta e código vêm da tela "Parear dispositivo com código de pareamento"
      em Config > Opções do desenvolvedor > Depuração sem fio.

  $(basename "$0") install <ip:porta_conexao> [caminho_do_apk]
      Conecta ao device já pareado e instala/atualiza o APK.
      IP:porta vem da tela principal de "Depuração sem fio" (muda a cada sessão).
      Se caminho_do_apk for omitido, usa o .apk mais recente na raiz do repo.
EOF
  exit 1
}

[[ $# -lt 2 ]] && usage

cmd="$1"
shift

case "$cmd" in
  pair)
    [[ $# -ne 2 ]] && usage
    adb pair "$1" "$2"
    ;;
  install)
    [[ $# -lt 1 ]] && usage
    ip_port="$1"
    apk="${2:-}"

    if [[ -z "$apk" ]]; then
      apk="$(ls -t "$REPO_ROOT"/OpenDisplay-android-*.apk 2>/dev/null | head -1)"
      [[ -z "$apk" ]] && { echo "Nenhum APK encontrado em $REPO_ROOT. Passe o caminho explicitamente." >&2; exit 1; }
    fi

    echo "Conectando em $ip_port..."
    adb connect "$ip_port"

    echo "Instalando $apk..."
    adb -s "$ip_port" install -r "$apk"
    ;;
  *)
    usage
    ;;
esac
