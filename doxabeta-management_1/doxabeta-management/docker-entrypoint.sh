#!/bin/sh
set -eu

if [ -n "${DATABASE_URL:-}" ] || [ -n "${JDBC_DATABASE_URL:-}" ] || [ -n "${SPRING_DATASOURCE_URL:-}" ]; then
  export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-postgres}"
fi

if [ -n "${DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ] && [ -z "${JDBC_DATABASE_URL:-}" ]; then
  uri="$DATABASE_URL"
  uri_no_scheme="${uri#postgres://}"
  uri_no_scheme="${uri_no_scheme#postgresql://}"
  auth_part="${uri_no_scheme%%/*}"
  db_part="${uri_no_scheme#*/}"
  host_port_part="${auth_part#*@}"
  user_pass_part="${auth_part%@*}"

  db_name="${db_part%%\?*}"
  user_name="${user_pass_part%%:*}"
  password_value="${user_pass_part#*:}"

  export JDBC_DATABASE_URL="jdbc:postgresql://${host_port_part}/${db_name}?user=${user_name}&password=${password_value}"
fi

if [ -n "${JDBC_DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  export SPRING_DATASOURCE_URL="$JDBC_DATABASE_URL"
fi

exec java -jar /app/app.jar
