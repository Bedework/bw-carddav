:: Run the bedework carddav tools programs

:: JAVA_HOME needs to be defined

@echo off
SETLOCAL ENABLEDELAYEDEXPANSION

if not "%JAVA_HOME%"=="" goto noJavaWarn
ECHO
ECHO
ECHO ***************************************************************************
ECHO          Warning: JAVA_HOME is not set - results unpredictable
ECHO ***************************************************************************
ECHO
ECHO
:noJavaWarn

SET cp=.;./classes;./resources
FOR /f %%i IN ('dir /b lib\*.jar') DO SET cp=!cp!;./lib/%%i

SET RUNCMD="%JAVA_HOME%\bin\java" -cp %cp% org.bedework.carddav.tools.Importer

SET APPNAME=carddavImp

ECHO   %RUNCMD% -appname %APPNAME% %2 %3 %4 %5 %6 %7 %8 %9
%RUNCMD% -appname %APPNAME% %2 %3 %4 %5 %6 %7 %8 %9

