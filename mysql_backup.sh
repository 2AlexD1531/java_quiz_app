#!/usr/bin/env bash
set -euo pipefail

# ---------- настройки ----------
CONTAINER="quiz_mysql"
DB="tester"
USER="root"
PASS="${DB_ROOT_PASS:-}"        # можно читать из .env при запуске скрипта
BACKUP_DIR="$HOME/backup"
RETAIN_DAYS=7

# ---------- подготовка ----------
mkdir -p "$BACKUP_DIR"
DATE=$(date +%F_%H-%M)
FILE="$BACKUP_DIR/${DB}_${DATE}.sql.gz"

# если пароль не задан – попробовать вытянуть из .env рядом с compose
if [[ -z "$PASS" ]] && [[ -f .env ]]; then
  PASS=$(grep -E '^DB_ROOT_PASS=' .env | cut -d= -f2-)
fi

# ---------- дамп + gzip ----------
echo "[$(date)] Начинаю дамп БД $DB ..."
docker exec -i "$CONTAINER" \
  mysqldump -u"$USER" -p"$PASS" --routines --triggers --single-transaction --lock-tables=false "$DB" \
  | gzip > "$FILE"

# ---------- ротация ----------
echo "[$(date)] Удаляю копии старше $RETAIN_DAYS дней ..."
find "$BACKUP_DIR" -name "${DB}_*.sql.gz" -mtime +$RETAIN_DAYS -delete

echo "[$(date)] Готово: $FILE"
