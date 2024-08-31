#!/bin/bash

BASE_URL="http://localhost:8080"
RATES_URL="${BASE_URL}/rates"
PERMITS_URL="${BASE_URL}/permits"
RATE_ID="test-rate"

function log() {
  printf "%s %s\n" "$(date +'%T')" "$1"
}

data="{ \"id\":\"${RATE_ID}\", \"rates\":[{ \"rate\":\"1/s\" }] }"
log ""
log "POST application/json ${data}"
log "Expected: ${data}"
output=$(curl -s -H 'Content-Type: application/json' \
      -X POST \
      -d "$data" \
      "${RATES_URL}")
log "  Actual: ${output}"

url="${RATES_URL}/${RATE_ID}"
log ""
log "GET ${url}"
log "Expected: ${data}"
output=$(curl -s "$url")
log "  Actual: ${output}"

url="${PERMITS_URL}/acquire?rateId=${RATE_ID}&permits=1"
log ""
log "PATCH ${url}"
log "Expected: true"
output=$(curl -s -X PATCH "${url}")
log "  Actual: ${output}"

url="${PERMITS_URL}/acquire?rateId=${RATE_ID}&permits=9"
log ""
log "PATCH ${url}"
log "Expected: {\"type\":\"about:blank\",\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\"Too many requests.\",\"instance\":\"uri=/permits/acquire\"}"
output=$(curl -s -X PATCH "${url}")
log "  Actual: ${output}"

url="${RATES_URL}/${RATE_ID}"
log ""
log "DELETE ${url}"
log "Expected: true"
output=$(curl -s -X DELETE "$url")
log "  Actual: ${output}"

