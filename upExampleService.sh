#!/bin/sh

docker-compose -f example-service-under-test/docker-compose.yaml down &&
docker-compose -f example-service-under-test/docker-compose.yaml build &&
docker-compose -f example-service-under-test/docker-compose.yaml up
