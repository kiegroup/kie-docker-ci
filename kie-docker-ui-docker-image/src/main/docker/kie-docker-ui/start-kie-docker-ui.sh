#!/bin/bash

# Set docker remote API private and public hosts using the given environment variables.
PROPS="-Ddocker.host.private=$KIE_DOCKER_SERVER_PRIVATE -Ddocker.host.public=$KIE_DOCKER_SERVER_PUBLIC -Ddocker.jenkins.url=$KIE_DOCKER_JENKINS_URL"

# File system path for deployed artifacts
if [ ! -z "$KIE_DOCKER_ARTIFACTS_PATH" ]; then
    PROPS="$PROPS -Ddocker.kie.artifacts.path=$KIE_DOCKER_ARTIFACTS_PATH"
fi

# Start Tomcat.
sh $JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 $PROPS
exit $?