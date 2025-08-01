# Release Notes

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased (6.1.0-SNAPSHOT)

## [6.0.1] - 2025-07-30
* A number of changes related to upgrading to jquery 3.x.

## [6.0.0] - 2025-07-19
* First jakarta release

## [5.0.7] - 2024-02-06
* Update library versions
* Preparing for jakarta...
  Update carddav and self-registration for jcache
* Move response classes and ToString into bw-base module.
* Switch to use DbSession from bw-database.
* Convert the hql queries into valid jpql. No hibernate specific terms were required (I think).

## [5.0.6] - 2024-12-09
* Update library versions

## [5.0.5] - 2024-12-06
* Update library versions
* 5.0.4 was missed

## [5.0.3] - 2024-11-26
* Update library versions

## [5.0.2] - 2024-09-18
* Update library versions
* Make webdavexception subclass of runtimeexception and tidy up a bit. Should be no noticable changes.

## [5.0.1] - 2024-03-22
* Use util-hibernate classes for hib session. Throws different exception...
* Simplify the configuration utilities.
* Fix up a bunch of introduced config errors
* Remove dependency on bw-xml deployment

## [5.0.0] - 2022-02-12
* Use bedework-parent for builds.
* Update library versions
* Move a bunch of vcard related code from carddav into common utility project
* Pass class loader as parameter when creating new objects. JMX interactions were failing.
* Update bw-util version to use new exception handling.
  Also update for new schema build code.

## [4.0.10] - 2020-03-22
* Upgrade jackson
* Move response objects out of CalFacade so they can be used within other modules.
* Provide webdav access to WdSysIntf object. Allows prefixing of uri in error response.

## [4.0.9] - 2019-10-16
* Update library versions.
* bw-util refactor

## [4.0.8] - 2019-08-27
* Update library versions.
* Switch to PooledHttpClient

## [4.0.7] - 2019-04-15
* Update library versions.

## [4.0.6] - 2019-01-07
* Update library versions.

## [4.0.5] - 2018-12-14
* Redo after release failed

## [4.0.4] - 2018-12-14
* Logging changes

## [4.0.3] - 2018-11-28
* Fixes to carddav to map from /public/people to /principals/users
* Fix handling of carddav "N" property when getting user details
* Try to fix the comparison for properties. They are supposed to be in a fixed order by name - however the name of wrapped x-properties is a parameter. Use that parameter for the comparison.
* Factor out hibernate related util classes
* Upgrade jackson

## [4.0.2] - 2018-04-08
* Copy some useful methods into AbstractDirHandler

## [4.0.1] - 2015-12-20
* Uncommitted files for carddav

## [4.0.0] - 2015-12-20
* Further refactoring of carddav to reduce the impact on configs. Moved implementations of configs back into server module.
* Fixed version numbering - should have been set to 4 initially

## [1.0.4] - 2015-12-18
* Fix pom for sonatype

## [1.0.3] - 2015-12-18
* First maven central release.
* Refactored to provide a common jar used for directory handler builds.
* Use post-build plugin to deploy in server.
 
## [1.0.2] - 2015-12-12
* First identifiable release.
* Very many preceding changes - see log