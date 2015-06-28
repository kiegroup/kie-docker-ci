#!/usr/bin/env bash

# Builds and run single application images and containers (for all application servers supported and both master/product) branches

# Arguments
#   - KIE Application to build (value must match the artifact id value for the maven module for the KIE application. Ex: "kie-wb", "kie-drools-wb", etc)

export MVN_HOME=/usr/local/maven/actVer
export JAVA_HOME=/usr/local/java/actVer
KIE_HOME=../
KIE_APP=
                  
function usage
{
    echo "usage: build_app.sh -k <application>"
}

while [ "$1" != "" ]; do
    case $1 in
        -home | --kie-home )    shift
                                KIE_HOME=$1
                                ;;
        -k | --kie-app )        shift
                                KIE_APP=$1
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     usage
                                exit 1
    esac
    shift
done                  

if [[ -z "$KIE_APP" ]] ; then
    echo "No application argument specified. Exiting.."
    usage
    exit 65
fi

# Clean containers and images for the specified KIE application.
sh $KIE_HOME/scripts/docker_clean_app.sh $KIE_APP

# Run the Maven build only for the specified KIE application.
echo "Performing Maven build for KIE application '$KIE_APP'..."
cd $KIE_HOME/ && $MVN_HOME/bin/mvn clean install -P !all,$KIE_APP
echo "Maven build finished..."

# Working directory and exit status.
exit $?