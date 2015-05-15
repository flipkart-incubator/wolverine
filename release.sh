#!/usr/bin/env bash
#mvn deploy -q -DperformRelease=true -DskipTests -DaltDeploymentRepository=flipkart::default::http://artifactory.nm.flipkart.com:8081/artifactory/libs-release-local
mvn deploy -q -DperformRelease=true -DskipTests -DaltDeploymentRepository=flipkart::default::http://artifactory.nm.flipkart.com:8081/artifactory/libs-releases-local

