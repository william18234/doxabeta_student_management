#!/bin/sh
set -eu

if [ -n "${DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ] && [ -z "${JDBC_DATABASE_URL:-}" ]; then
  export JDBC_DATABASE_URL="$(printf '%s' "$DATABASE_URL" | sed -E 's#^postgres(ql)?://#jdbc:postgresql://#')"
fi

if [ -n "${JDBC_DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  export SPRING_DATASOURCE_URL="$JDBC_DATABASE_URL"
fi

exec java -jar /app/app.jar
