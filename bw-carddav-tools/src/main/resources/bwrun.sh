#!/bin/sh

# Run the bedework carddav tools programs

# JAVA_HOME needs to be defined

cp=.:./classes:./resources

for i in lib/*
  do
    cp=$cp:$i
done

RUNCMD="$JAVA_HOME/bin/java -cp $cp org.bedework.c"

APPNAME=@BW-APP-NAME@

echo $RUNCMD $*
$RUNCMD  $*

