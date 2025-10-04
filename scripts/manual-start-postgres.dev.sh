#!/usr/bin/env bash

docker run --name=snippet_db --rm -p 5434:5432 \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_USER=sa \
  -e POSTGRES_DB=snippet_db \
  postgres:16