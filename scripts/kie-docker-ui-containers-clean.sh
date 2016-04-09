#!/bin/sh

# stop and remove current running Docker containers with the kie-docker-ui-webapp
echo "Stopping all kie-docker-ui containers"
docker ps | grep kie-docker-ui | awk '{print $1}' | xargs -r docker stop
echo "Removing all kie-docker-ui containers"
docker ps -a | grep kie-docker-ui | awk '{print $1}' | xargs -r docker rm -f
