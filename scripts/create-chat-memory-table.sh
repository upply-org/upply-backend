#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_ROOT"

if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs) 2>/dev/null || true
else
    echo "Error: .env file not found"
    exit 1
fi

DB_URL_WITHOUT_PREFIX="${DB_URL#jdbc:mysql://}"
DB_HOST="${DB_URL_WITHOUT_PREFIX%%:*}"
DB_PORT="${DB_URL_WITHOUT_PREFIX#*:}"
DB_PORT="${DB_PORT%%/*}"
DB_NAME="${DB_URL_WITHOUT_PREFIX#*/}"
DB_NAME="${DB_NAME%%\?*}"

echo "Connecting to MySQL at $DB_HOST:$DB_PORT database: $DB_NAME"

mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" < "$SCRIPT_DIR/create-chat-memory-table.sql"

echo "Table SPRING_AI_CHAT_MEMORY created successfully"
