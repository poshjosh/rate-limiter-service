#!/usr/bin/env bash

set -euo pipefail

cd .. || exit 1

docker-compose down

# We got port already in use a couple of times.
# So we sleep between shutdown and startup.
sleep 3

set -a
source .env
set +a

# To log output of the build process.
export BUILDKIT_PROGRESS=plain

# APP_VERSION must match the version in pom.xml
docker compose build --no-cache --build-arg APP_VERSION=0.0.1-SNAPSHOT

docker compose up

