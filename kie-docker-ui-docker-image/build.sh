#!/usr/bin/env bash

# fail fast
set -e

echo "Building kie-docker-ui..."
KIE_ARGUMENTS=" -Dkie.dockerui.privateHost=kieci-02.lab.eng.brq.redhat.com -Dkie.dockerui.publicHost=kieci-02.lab.eng.brq.redhat.com "

mvn clean install $KIE_ARGUMENTS