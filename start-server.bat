@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot
set PATH=%JAVA_HOME%\bin;C:\Swordie\apache-maven-3.9.14\bin;%PATH%
cd /d C:\Swordie\swordie-232
echo Starting Swordie server via Maven...
mvn exec:java -Dexec.mainClass="net.swordie.ms.Server"
