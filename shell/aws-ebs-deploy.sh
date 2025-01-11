#!/usr/bin/env bash

set -euo pipefail

APP_NAME=${APP_NAME:-rate-limiter-service}
AWS_EBS_ENV=${AWS_EBS_ENV:-"${APP_NAME}-env"}

cd ..

eb deploy "${AWS_EBS_ENV}"

eb status