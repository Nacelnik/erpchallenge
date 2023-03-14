#!/usr/bin/env bash
mvn -e clean package dockerfile:build
docker-compose up