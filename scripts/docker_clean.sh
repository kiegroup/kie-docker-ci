#!/usr/bin/env bash

# Stop and remove all running and exited containers.
echo "Stopping current running docker containers..."
docker stop $(docker ps -a -q)
echo "Remove current KIE running docker containers..."
docker ps -a | grep "jboss-kie" | awk '{print $1}' | xargs -r docker rm -f
echo "Remove current MySQL running docker containers..."
docker ps -a | grep "mysql" | awk '{print $1}' | xargs -r docker rm -f
echo "Remove current PostgreSQL running docker containers..."
docker ps -a | grep "postgres" | awk '{print $1}' | xargs -r docker rm -f

# Remove temporary built images.
echo "Removing temporary built images..."
docker images --no-trunc | grep none | awk '{print $3}' | xargs -r docker rmi -f

# Remove all images from jboss-kie* repository older than a week.
echo "Removing images that belongs to jboss-kie repository present for more than one week..."
docker images --no-trunc | grep "jboss-kie" | grep "week" | awk '{print $3}' | xargs -r docker rmi -f

echo "Removing all kie-docker-ui images..."
docker images --no-trunc | grep "kie-docker-ui" | awk '{print $3}' | xargs -r docker rmi -f

# Exit with last docker rmi command status
exit $?