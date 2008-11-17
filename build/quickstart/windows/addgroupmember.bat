::  This file is included by the quickstart script file "addgroupmember.bat"
::  so that we may keep this script under version control in the svn repository.

@ECHO off
SETLOCAL

  ECHO.
  ECHO.
  ECHO   Bedework Calendar System
  ECHO   ------------------------
  ECHO.

  SET PRG=%0
  SET saveddir=%CD%
  SET QUICKSTART_HOME=%saveddir%
  SET ANT_HOME=%QUICKSTART_HOME%\apache-ant-1.7.0

  SET group=%1
  SET groupmember=%2

  IF "%group%" == "help" GOTO usage
  IF "%group%empty" == "empty" GOTO errorUsage
  IF "%groupmember%empty" == "empty" GOTO errorUsage

  IF NOT "%JAVA_HOME%empty" == "empty" GOTO javaOk
  ECHO    *******************************************************
  ECHO    Error: JAVA_HOME is not defined correctly for Bedework.
  ECHO    *******************************************************
  GOTO usage

:javaOk
  SET CLASSPATH=%ANT_HOME%\lib\ant-launcher.jar
  SET ant_home_def=-Dant.home=%ANT_HOME%
  SET ant_class_def=org.apache.tools.ant.launch.Launcher

  SET addgroup_defs=-Dorg.bedework.directory.group=%group%
  SET addgroup_defs=%addgroup_defs% -Dorg.bedework.directory.group.member=%groupmember%

  "%JAVA_HOME%\bin\java" -classpath "%CLASSPATH%" %ant_home_def% %addgroup_defs% %ant_class_def% addGroupMember
  GOTO:EOF

:errorUsage
  ECHO    *******************************************************
  ECHO    Error: You must supply a group name and a group member.
  ECHO    *******************************************************

:usage
  ECHO.
  ECHO    Usage:
  ECHO.
  ECHO    %PRG% group account
  ECHO.
  ECHO    Invokes ant to build the Bedework tools then uses that tool to add
  ECHO    a group member to the directory.
  ECHO.
  ECHO.
