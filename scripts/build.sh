#!/usr/bin/env bash

# Procedure:
# 1.- Clean containers and images from jboss-kie repository
# 1.- Restart the Docker daemon (as it sometimes hangs...)
# 3.- Run the Maven build
#     3.0.- The Maven build for the module 'kie-docker-ui-app' is disabled by default, unless specifying it with the script argument provided
#     3.1.- Build all kie images (kie-wb, drools-wb, kie-server, uf-dashbuilder etc) images for both maste and product branches
#     3.2.- Run some of them by default
#     3.3.- Run kie-docker-ui

# Arguments
#   - KIE docker integration repository home path
#   - Path for the Maven settings.xml to use
#   - KIE version to use for master branch artifacts
#   - KIE version to use for product branch artifacts
#   - Argument for enabling 'kie-docker-ui-app' module on Maven build.
#   - Profile to use for the Maven build
#   - Arguments for running the KIE Docker UI Docker container:
#       - Private host
#       - Public host
#       - Artifacts path
#       - Jenkins job URL

KIE_HOME=../
KIE_SETTINGS_PATH=$MVN_HOME/conf/settings.xml
KIE_MASTER_VERSION=6.3.0-SNAPSHOT
KIE_PRODUCT_VERSION=6.2.1-SNAPSHOT
DASH_MASTER_VERSION=0.3.0-SNAPSHOT
KIE_PROFILE=all
KIE_DOCKER_UI_APP_BUILD=no
KIE_HOST_PRIVATE=
KIE_HOST_PUBLIC=
KIE_ARTIFACTS_PATH=
KIE_JENKINS_URL=
                  
function usage
{
    echo "usage: build_all.sh -s <settings_path> -mv <master_branch_version> -pv <product_branch_version>  -ui-app <no|yes> -p <maven_profile> -hpriv <docker_host_private> -hpub <docker_host_public> -a <kie_artifacts_path> -j <jenkins_url>"
}

while [ "$1" != "" ]; do
    case $1 in
        -home | --kie-home)     shift
                                KIE_HOME=$1
                                ;;
        -s | --settings)        shift
                                KIE_SETTINGS_PATH=$1
                                ;;
        -kmv | --kie-master-version) shift
                                KIE_MASTER_VERSION=$1
                                ;;
        -kpv | --kie-product-version) shift
                                KIE_PRODUCT_VERSION=$1
                                ;;
        -dpv | --dash-product-version) shift
                                DASH_MASTER_VERSION=$1
                                ;;
        -p | --profile )        shift
                                KIE_PROFILE=$1
                                ;;
        -ui-app | --kie-docker-ui-pp)   shift
                                        KIE_DOCKER_UI_APP_BUILD=$1
                                        ;;

        -hpriv | --docker-host-private )   shift
                                KIE_HOST_PRIVATE=$1
                                ;;
        -hpub | --docker-host-public )    shift
                                KIE_HOST_PUBLIC=$1
                                ;;
        -a | --artifacts-path ) shift
                                KIE_ARTIFACTS_PATH=$1
                                ;;
        -j | --jenkins-url )    shift
                                KIE_JENKINS_URL=$1
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done                  

KIE_MAVEN_ARGUMENTS=" -s $KIE_SETTINGS_PATH "
KIE_MAVEN_PROFILES=""

# Set the profiles to use for the Maven build.
if [ "$KIE_PROFILE" == "all" ]; then
    KIE_MAVEN_PROFILES="all"
else 
    KIE_MAVEN_PROFILES="!all,$KIE_PROFILE"
fi

# Check if the kie-docker-ui-app must be build.
if [ "$KIE_DOCKER_UI_APP_BUILD" == "yes" ]; then
    KIE_MAVEN_PROFILES="$KIE_MAVEN_PROFILES,!kie-docker-ci-only,kie-docker-ci-all"
fi

KIE_MAVEN_ARGUMENTS=" $KIE_MAVEN_ARGUMENTS -P $KIE_MAVEN_PROFILES"

# Version for master and product branch artifacts.
KIE_ARGUMENTS=" -Ddocker.kie.master.version=$KIE_MASTER_VERSION -Ddocker.kie.product.version=$KIE_PRODUCT_VERSION -Ddocker.dashbuilder.master.version=$DASH_MASTER_VERSION "

# Private host argument (for running KIE Docker UI Docker oontainer)
if [[ -z "$KIE_HOST_PRIVATE" ]] ; then
    echo "No private host argument specified. Exiting.."
    exit 65
fi

# Public host argument (for running KIE Docker UI Docker oontainer)
if [[ -z "$KIE_HOST_PUBLIC" ]] ; then
    echo "No public host argument specified. Exiting.."
    exit 65
fi

KIE_ARGUMENTS="$KIE_ARGUMENTS -Dkie.dockerui.privateHost=$KIE_HOST_PRIVATE -Dkie.dockerui.publicHost=$KIE_HOST_PUBLIC "

# Clean containers and jboss-kie images older than a week.
sh $KIE_HOME/scripts/docker_clean.sh

# Clean previously deployed artifacts
sh $KIE_HOME/scripts/artifacts_clean.sh $KIE_ARTIFACTS_PATH
KIE_ARGUMENTS=" $KIE_ARGUMENTS -Dkie.artifacts.deploy.path=$KIE_ARTIFACTS_PATH "

# Jenkins URL
if [[ ! -z "$KIE_JENKINS_URL" ]] ; then
    KIE_ARGUMENTS=" $KIE_ARGUMENTS -Dkie.dockerui.jenkinsURL=$KIE_JENKINS_URL "
fi

# Restart the service avoiding leaking issues...
sudo service docker restart

# Run the Maven build.
echo "Performing build for all KIE applications using arguments: '$KIE_MAVEN_ARGUMENTS $KIE_ARGUMENTS'..."
cd $KIE_HOME/ && $MVN_HOME/bin/mvn clean install $KIE_MAVEN_ARGUMENTS $KIE_ARGUMENTS
echo "Build finished!"

# Working directory and exit status.
exit $?