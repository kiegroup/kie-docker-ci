/**
 * Copyright (C) 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.dockerui.client.resources.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.ConstantsWithLookup;

public interface Constants extends ConstantsWithLookup {

    public static final Constants INSTANCE = GWT.create( Constants.class );

    String dockerContinuousIntegration();
    String goToJenkins();
    String home();
    String containers();
    String artifacts();
    String categories();
    String createContainer();
    String kieFooter();
    String back();
    String refresh();
    String noContainers();
    String noImages();
    String allCategories();
    String loading();
    String containerCount();
    String containerStatus();
    String containerId();
    String containerName();
    String containerCreationDate();
    String containerImage();
    String containerCommand();
    String containerIP();
    String containerPorts();
    String privatePort();
    String publicPort();
    String noPorts();
    String containerEnvVars();
    String noEnvVars();
    String name();
    String value();
    String version();
    String stop();
    String start();
    String restart();
    String remove();
    String viewLogs();
    String viewDetails();
    String navigate();
    String actions();
    String containerStarted();
    String containerStopped();
    String containerRestarted();
    String containerRemoved();
    String logsForContainer();
    String detailsForContainer();
    String containerDetails();
    String sshConnectionCommand();
    String webAddressUrl();
    String artifactVersions();
    String jdbcInformation();
    String jdbcUrl();
    String user();
    String password();
    String pullCommand();
    String pull();
    String download();
    String downloadWAR();
    String noPublicPortsAvailable();
    String noLogs();
    String selectKieAppType();
    String selectAppServerType();
    String selectDbmsType();
    String selectTagAndEnvs();
    String imageSelected();
    String dbImageSelected();
    String specifyContainerName();
    String specifyDbContainerName();
    String selectTag();
    String selectContainerName();
    String setEnvs();
    String tag();
    String tags();
    String cancel();
    String run();
    String add();
    String newKey();
    String newValue();
    String noData();
    String key();
    String inMemoryDB();
    String noImageRequiredForDatabase();
    String next();
    String containerCreatedWithId();
    String containerStartedWithId();
    String imageToStart();
    String useContainerName();
    String dbImageToStart();
    String useDbContainerName();
    String newContainerCreation();
    String runningKieContainer();
    String runningDbContainer();
    String databaseCreatedSuccess();
    String connectionDetails();
    String databaseJdbcUrl();
    String databaseUser();
    String databasePassword();
    String databaseName();
    String createAndStartDatabaseContainer();
    String createKieDatabase();
    String createAndStartKieContainer();
    String finished();
    String nextRefresh();
    String refreshPlay();
    String refreshStop();
    String newLiteral();
    String createNew();
    String createNewForThisType();
    String noImagesForCreateContainer();
    String createContainerOfImage();
    String notAvailable();
    String availableImages();
    String images();
    String image();
    String imageId();
    String imageRepository();
    String imageCreationDate();
    String imageTaggedDate();
    String noDateTagInfo();
    String versionDetails();
    String view();
    String timeoutFired();
    String status();
    String statusRunnable();
    String statusNotRunnable();
    String statusNotEvaluated();
    String statusNotApplicable();
    String kieImagesByDate();
    String dblClickToExplore();
    String clickForUpdate();
    String reloadStatusCompleted();
    String imagesCount();
    String containersCount();
    String availableKieImages();
    String created();
    String systemSummary();
    String kieSummary();
    String fileName();
    String timestamp();
    String artifactId();
    String artifactVersion();
    String artifactType();
    String artifactClassifier();
    String artifactsViewHeader();
    String searchArtifacts();
    String searchFileName();
    String search();
    String cleanSearch();
    String containerIsDown();
    String runningContainersCount();
}
