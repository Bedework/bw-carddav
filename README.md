## bw-carddav [![Build Status](https://travis-ci.org/Bedework/bw-carddav.svg)](https://travis-ci.org/Bedework/bw-carddav)

A carddav server which provides a gateway to a number of directories using
directory handler classes selected by resource path prefixes.

### Requirements

1. JDK 7
2. Maven 3

### Building Locally

> mvn clean install

### Releasing

Releases of this fork are published to Maven Central via Sonatype.

To create a release, you must have:

1. Permissions to publish to the `org.bedework` groupId.
2. `gpg` installed with a published key (release artifacts are signed).

To perform a new release:

> mvn release:clean release:prepare

When prompted, select the desired version; accept the defaults for scm tag and next development version.
When the build completes, and the changes are committed and pushed successfully, execute:

> mvn release:perform

For full details, see [Sonatype's documentation for using Maven to publish releases](http://central.sonatype.org/pages/apache-maven.html).

### Deploying

To deploy into an application server requires a deploy.properties. The
path to such  file can be set in your profile by defining the property

 org.bedework.deployment.properties

Then:
  * cd bw-carddav-ear
  * mvn org.bedework:bw-deploy-maven-plugin:4.0.4:deploy-ears

### Release Notes
#### 1.0.3
  * First maven central release.
  * Refactored to provide a common jar used for directory handler builds.
  * Use post-build plugin to deploy in server.
#### 1.0.4
  * Fix pom for sonatype

#### 4.0.1
  * Further refactoring of carddav to reduce the impact on configs. Moved implementations of configs back into server module.
  * Fixed version numbering - should have been set to 4 initially

#### 4.0.2
  * Copy some useful methods into AbstractDirHandler