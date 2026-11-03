#!/usr/bin/env bash
# Genera la copia STATICA del sito (per GitHub Pages) a partire dal sito vero.
#
# Prerequisito: lo stack Docker acceso (docker compose up -d), perché le pagine prendono i dati
# da profile-service e projects-service.
# Uso:   scripts/export-static.sh
# Output: build/static-site/   (non pubblica niente: per pubblicare vedi scripts/publish-pages.sh)
set -euo pipefail

BASE_PATH="${BASE_PATH:-/sito-j}"                                   # sottocartella su GitHub Pages
PUBLIC_URL="${PUBLIC_URL:-https://jesusalvarado2005.github.io/sito-j}"
OUT="build/static-site"
PORT=8090
NAME=sitoj-static-export

cd "$(dirname "$0")/.."

# 1) web-public in modalità statica, nella rete Docker del sito (così raggiunge i servizi)
docker rm -f "$NAME" >/dev/null 2>&1 || true
docker run -d --rm --name "$NAME" --network sito-j_default -p "127.0.0.1:$PORT:8080" \
  -e SITE_STATIC_MODE=true \
  -e SERVER_SERVLET_CONTEXT_PATH="$BASE_PATH" \
  -e SITE_BASE_URL="$PUBLIC_URL" \
  -e PROFILE_SERVICE_URL=http://profile-service:8081 \
  -e PROJECTS_SERVICE_URL=http://projects-service:8082 \
  -e CONTACT_SERVICE_URL=http://contact-service:8083 \
  -e TURNSTILE_SITE_KEY=non-usata-in-modalita-statica \
  ghcr.io/jesusalvarado2005/sito-j-web-public:latest >/dev/null
trap 'docker rm -f "$NAME" >/dev/null 2>&1 || true' EXIT

echo "Attendo web-public..."
for _ in $(seq 1 60); do
  curl -fs "http://127.0.0.1:$PORT$BASE_PATH/it" >/dev/null 2>&1 && break
  sleep 2
done

# 2) pagine HTML: ogni URL diventa una cartella con index.html (/it -> it/index.html)
rm -rf "$OUT"
mkdir -p "$OUT"
for page in it en it/privacy en/privacy; do
  mkdir -p "$OUT/$page"
  curl -fsS "http://127.0.0.1:$PORT$BASE_PATH/$page" -o "$OUT/$page/index.html"
done
curl -fsS "http://127.0.0.1:$PORT$BASE_PATH/sitemap.xml" -o "$OUT/sitemap.xml"

# controllo: nelle pagine non deve esserci il form, che senza backend non funzionerebbe
if grep -q 'id="contactForm"' "$OUT"/it/index.html "$OUT"/en/index.html; then
  echo "ERRORE: il form è presente nella copia statica" >&2
  exit 1
fi

# 3) file statici (CSS, JS, font, logo, CV): gli stessi serviti da web-public
cp -r web-public/src/main/resources/static/. "$OUT/"

# 4) pagina iniziale: sceglie la lingua del browser (come fa "/" nel sito vero)
cat > "$OUT/index.html" <<EOF
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>Jeanfranco Alvarado — Junior Backend Developer</title>
<link rel="canonical" href="$PUBLIC_URL/en/">
<meta http-equiv="refresh" content="0; url=$BASE_PATH/en/">
<script>location.replace('$BASE_PATH/' + ((navigator.language || '').toLowerCase().startsWith('it') ? 'it' : 'en') + '/');</script>
</head>
<body><a href="$BASE_PATH/it/">Italiano</a> · <a href="$BASE_PATH/en/">English</a></body>
</html>
EOF

# GitHub Pages: niente elaborazione Jekyll (servono i file così come sono)
touch "$OUT/.nojekyll"

echo "Fatto: $OUT"
find "$OUT" -name index.html | sort
