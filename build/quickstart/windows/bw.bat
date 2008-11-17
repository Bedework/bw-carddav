::  This file is included by the quickstart script file "..\..\bw.bat" so that
::  we may keep this script under version control in the svn repository.

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

:: Default some parameters
SET BWCONFIGS=
SET bwc=default
SET BWCONFIG=
SET offline=
SET quickstart=

:: check for command-line arguments and branch on them
IF "%1noargs" == "noargs" GOTO usage
GOTO branch

:quickstart
  ECHO     Preparing quickstart build ...
  SET quickstart="yes"
  SHIFT
  GOTO branch

:bwchome
  :: Define location of configs
  SHIFT
  SET BWCONFIGS=%1
  SHIFT
  GOTO branch

:bwc
  SHIFT
  SET bwc=%1
  SHIFT
  GOTO branch

:offline
  ECHO     Setting to offline mode; libraries will not be downloaded ...
  SET offline="-Dorg.bedework.offline.build=yes"
  SHIFT
  GOTO branch

:doneWithArgs

IF NOT "%quickstart%empty" == "empty" GOTO checkBwConfig
IF NOT "%BWCONFIGS%empty" == "empty" GOTO DoneQB
SET BWCONFIGS=%HOME%\bwbuild
GOTO doneQB

:checkBwConfig
REM  IF "%BWCONFIGS%empty" == "empty" GOTO doneQB
REM    ECHO *******************************************************
REM    ECHO Error: Cannot specIFy both -quickstart and -bwchome
REM    ECHO *******************************************************
REM    GOTO:EOF

  SET BWCONFIGS=%QUICKSTART_HOME%\bedework\config\bwbuild

:doneQB
  SET BEDEWORK_CONFIGS_HOME=%BWCONFIGS%
  SET BEDEWORK_CONFIG=%BWCONFIGS%\%bwc%

  IF EXIST "%BEDEWORK_CONFIG%\build.properties" GOTO foundBuildProperties
  ECHO *******************************************************
  ECHO Error: Configuration %BEDEWORK_CONFIG%
  ECHO does not exist or is not a Bedework configuration.
  ECHO *******************************************************
  GOTO:EOF
:foundBuildProperties

  IF NOT "%JAVA_HOME%empty"=="empty" GOTO javaOk
  ECHO *******************************************************
  ECHO Error: JAVA_HOME is not defined correctly for Bedework.
  ECHO *******************************************************
  GOTO:EOF
:javaOk

:runBedework
  :: Make available for ant
  SET BWCONFIG=-Dorg.bedework.user.build.properties=%BEDEWORK_CONFIG%\build.properties

  ECHO.
  ECHO     BWCONFIGS = %BWCONFIGS%
  ECHO     BWCONFIG = %BWCONFIG%
  ECHO.

  SET ANT_HOME=%QUICKSTART_HOME%\apache-ant-1.7.0

  SET CLASSPATH="%ANT_HOME%\lib\ant-launcher.jar"

  "%JAVA_HOME%\bin\java.exe" -classpath %CLASSPATH% %offline% -Dant.home=%ANT_HOME% org.apache.tools.ant.launch.Launcher %BWCONFIG% %1

  GOTO:EOF


:: Iterate over the command line arguments;
:: DOS Batch labels can't contain hyphens, so convert them
:: (otherwise, we could just "GOTO %1")
:branch
IF "%1" == "-quickstart" GOTO quickstart
IF "%1" == "-bwchome" GOTO bwchome
IF "%1" == "-bwc" GOTO bwc
IF "%1" == "-offline" GOTO offline
GOTO doneWithArgs

:usage
  ECHO    Usage:
  ECHO.
  ECHO    %0 [ -quickstart OR -bwchome path ] [ -bwc configname ] [ -offline ] [ target ]
  ECHO.
  ECHO      -quickstart  Use the current quickstart configurations.
  ECHO      -bwchome     Specify path to configurations
  ECHO      -bwc         Specify configuration name
  ECHO      -offline     Build without atempting to retrieve library jars
  ECHO      target       Ant target to execute
  ECHO.
  ECHO    Invokes ant to build or deploy the Bedework system. Uses a configuration
  ECHO    directory which contains one directory per configuration.
  ECHO.
  ECHO    Within each configuration directory we expect a file called
  ECHO    build.properties which should point to the property and options file
  ECHO    needed for the deploy process.
  ECHO.
  ECHO    In general these files will be in the same directory as build.properties.
  ECHO    The environment variable BEDEWORK_CONFIG contains the path to the current
  ECHO    configuration directory and can be used to build a path to the other files.
  ECHO.
  ECHO.
