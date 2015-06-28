/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.dockerui.client.resources.bundles;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;

public interface Images extends ClientBundle {

    public static final Images INSTANCE = GWT.create(Images.class);
    
    @Source("images/docker.png")
    DataResource docker();

    @Source("images/docker_icon.png")
    DataResource dockerIcon();

    @Source("images/drools.png")
    DataResource drools();

    @Source("images/drools_icon.png")
    DataResource droolsIcon();

    @Source("images/h2.png")
    DataResource h2();

    @Source("images/jboss.png")
    DataResource jboss();

    @Source("images/jboss-eap.png")
    DataResource jbossEAP();

    @Source("images/kie.png")
    DataResource kie();

    @Source("images/kie-ide.png")
    DataResource kieIde();

    @Source("images/list_icon.jpg")
    DataResource listIcon();

    @Source("images/mysql.png")
    DataResource mysql();

    @Source("images/play_icon.png")
    ImageResource playIcon();

    @Source("images/play_icon.png")
    DataResource playIconData();

    @Source("images/logs_icon.png")
    ImageResource logsIcon();

    @Source("images/details_icon.png")
    ImageResource detailsIcon();

    @Source("images/postgresql.png")
    DataResource postgresql();

    @Source("images/reload_icon.png")
    ImageResource reloadIconBlue();

    @Source("images/reload_icon_black.png")
    ImageResource reloadIconBlack();

    @Source("images/remove_icon.png")
    ImageResource removeIcon();

    @Source("images/stop_icon.png")
    ImageResource stopIcon();

    @Source("images/stop_icon.png")
    DataResource stopIconData();
    
    @Source("images/internet_icon.png")
    ImageResource internetIcon();

    @Source("images/text_icon.jpg")
    DataResource textIcon();

    @Source("images/thumbnails_icon.png")
    DataResource thumbnailsIcon();

    @Source("images/tomcat.png")
    DataResource tomcat();

    @Source("images/wildfly.png")
    DataResource wildfly();

    @Source("images/number_one.png")
    DataResource numberOne();

    @Source("images/number_two.png")
    DataResource numberTwo();

    @Source("images/number_three.png")
    DataResource numberThree();

    @Source("images/number_four.png")
    DataResource numberFour();

    @Source("images/number_five.png")
    DataResource numberFive();
    
    @Source("images/circle_green_icon.png")
    ImageResource circleGreenIcon();
    
    @Source("images/circle_red_icon.png")
    ImageResource circleRedIcon();

    @Source("images/green_tick.png")
    ImageResource greenTick();

    @Source("images/jenkins.png")
    ImageResource jenkins();

    @Source("images/dashbuilder_logo.png")
    DataResource dashbuilderLogo();

    @Source("images/docker_containers.png")
    DataResource dockerContainers();
    
}
