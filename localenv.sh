#!/bin/bash

docker rm -f rfr-mysql 2>/dev/null || true

docker compose up -d mysql --wait