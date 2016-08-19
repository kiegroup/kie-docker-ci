####### BASE ############
FROM jboss/wildfly:10.0.0.Final

####### MAINTAINER ############
MAINTAINER "Petr Široký" "psiroky@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -Xmx1g -Djava.security.egd=file:/dev/urandom
ENV KIE_DOCKER_SERVER_PRIVATE 172.17.42.1
ENV KIE_DOCKER_SERVER_PUBLIC kieci-02.lab.eng.brq.redhat.com
ENV KIE_DOCKER_JENKINS_URL https://kie-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/Docker/

####### WEBAPP ############
ADD maven/kie-docker-ui-webapp.war $JBOSS_HOME/standalone/deployments/

###### START SCRIPT #########
ADD start-kie-docker-ui.sh $JBOSS_HOME/bin/start-kie-docker-ui.sh

####### RUNNING KIE DOCKER UI ############
WORKDIR $JBOSS_HOME/bin
CMD ["./start-kie-docker-ui.sh"]
