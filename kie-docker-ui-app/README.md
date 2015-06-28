KIE Docker UI
-------------

This maven module contains sources for the **KIE Docker UI** web application, which is a custom web application built specific for managing all KIE and Docker continuous integration stuff provided by this repository.                       
 
This web application is build using GWT 2.7 and uses the Docker REST API for managing images and containers by the [Docker Java Maven plugin](https://github.com/docker-java/docker-java/).                     
 
NOTE: This web application is NOT a generic Docker service management application, as it's customized for KIE images and provides features to facilitate KIE integration tests, alhogut you can manage any kind of Docker image/container, see the logs, etc.                         