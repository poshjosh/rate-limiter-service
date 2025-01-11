#!/usr/bin/env bash

set -euo pipefail

APP_NAME=${APP_NAME:-rate-limiter-service}
AWS_EBS_ENV=${AWS_EBS_ENV:-"${APP_NAME}-env"}
AWS_REGION=${APP_ENV:-us-east-2}
JAVA_PLATFORM=${JAVA_PLATFORM:-corretto-17}

cd ..

###############################################################
# Make sure you have the Elastic Beanstalk CLI installed
###############################################################
# pip install awsebcli

eb init "${APP_NAME}" --platform "${JAVA_PLATFORM}" --region "${AWS_REGION}"

eb create "${AWS_EBS_ENV}"

eb status