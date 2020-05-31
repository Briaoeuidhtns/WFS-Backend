FROM postgres:12

COPY target/model.sql /docker-entrypoint-initdb.d/00-model.sql

COPY test/data.sql /docker-entrypoint-initdb.d/01-data.sql
