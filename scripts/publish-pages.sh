#!/usr/bin/env bash
# Pubblica build/static-site sul ramo gh-pages (quello che GitHub Pages mostra).
# Prima generare la copia con scripts/export-static.sh.
#
# Lavora in una cartella temporanea separata (git worktree): la tua cartella di lavoro e il ramo main non vengono toccati.
# Nel ramo gh-pages i file vecchi si tolgono con "git rm" (solo file tracciati da Git in quel ramo), poi si copiano i nuovi.
set -euo pipefail
cd "$(dirname "$0")/.."

SRC="build/static-site"
[ -f "$SRC/it/index.html" ] || { echo "Manca $SRC: lancia prima scripts/export-static.sh" >&2; exit 1; }

TMP="$(mktemp -d)"
cleanup() { git worktree remove --force "$TMP" >/dev/null 2>&1 || true; }
trap cleanup EXIT

if git ls-remote --exit-code --heads origin gh-pages >/dev/null 2>&1; then
  git fetch -q origin gh-pages
  git worktree add -q -B gh-pages "$TMP" origin/gh-pages
  git -C "$TMP" rm -r -q --ignore-unmatch .
else
  git worktree add -q --orphan -b gh-pages "$TMP"   # primo deploy: ramo nuovo e vuoto
fi

cp -r "$SRC/." "$TMP/"
git -C "$TMP" add -A
if git -C "$TMP" diff --cached --quiet; then
  echo "Nessuna modifica da pubblicare."
  exit 0
fi
git -C "$TMP" commit -q -m "Pubblica la copia statica del sito ($(git rev-parse --short HEAD))"
git -C "$TMP" push -q origin gh-pages
echo "Pubblicato su gh-pages. Tra 1-2 minuti: https://jesusalvarado2005.github.io/sito-j/"
