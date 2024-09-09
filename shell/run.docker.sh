#!/usr/bin/env bash

set -euo pipefail

cd .. || exit 1

docker-compose down

# We got port already in use a couple of times.
# So we sleep between shutdown and startup.
sleep 3

# To log output of the build process.
export BUILDKIT_PROGRESS=plain

docker compose up -d --build

