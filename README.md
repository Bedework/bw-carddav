# bw-carddav [![Build Status](https://travis-ci.org/Bedework/bw-carddav.svg)](https://travis-ci.org/Bedework/bw-carddav)

A carddav server which provides a gateway to a number of directories using
directory handler classes selected by resource path prefixes.

## Requirements

1. JDK 11
2. Maven 3

## Building Locally

> mvn clean install

## Releasing

Releases of this fork are published to Maven Central via Sonatype.

To create a release, you must have:

1. Permissions to publish to the `org.bedework` groupId.
2. `gpg` installed with a published key (release artifacts are signed).

To perform a new release:

> mvn -P bedework-dev release:clean release:prepare

When prompted, select the desired version; accept the defaults for scm tag and next development version.
When the build completes, and the changes are committed and pushed successfully, execute:

> mvn -P bedework-dev release:perform

For full details, see [Sonatype's documentation for using Maven to publish releases](http://central.sonatype.org/pages/apache-maven.html).

## Deploying

To deploy into an application server requires a deploy.properties. The
path to such  file can be set in your profile by defining the property

 org.bedework.deployment.properties

Then:
  * cd bw-carddav-ear
  * mvn org.bedework:bw-deploy-maven-plugin:4.0.4:deploy-ears

## Release Notes
### 1.0.3
  * First maven central release.
  * Refactored to provide a common jar used for directory handler builds.
  * Use post-build plugin to deploy in server.
### 1.0.4
  * Fix pom for sonatype

### 4.0.1
  * Further refactoring of carddav to reduce the impact on configs. Moved implementations of configs back into server module.
  * Fixed version numbering - should have been set to 4 initially

### 4.0.2
  * Copy some useful methods into AbstractDirHandler

### 4.0.3
    * Fixes to carddav to map from /public/people to /principals/users
    * Fix handling of carddav "N" property when getting user details
    * Try to fix the comparison for properties. They are supposed to be in a fixed order by name - however the name of wrapped x-properties is a parameter. Use that parameter for the comparison.
    * Factor out hibernate related util classes
    * Upgrade jackson

### 4.0.4
    * Logging changes

### 4.0.5
    * Logging changes

### 4.0.6
    * Dependencies

### 4.0.7
    * Upgrade jackson
    * Java 11 changes

### 4.0.8
    * Switch to PooledHttpClient

### 4.0.9
    * Upgrade jackson
    * bw-util refactor
    
  
  