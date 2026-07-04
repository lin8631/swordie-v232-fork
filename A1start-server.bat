@echo off
set JAVA_HOME=%~dp0JDK 21_WIN\jdk-21.0.11+10
set PATH=%JAVA_HOME%\bin;%~dp0JDK 21_WIN\apache-maven-3.9.9\bin;%PATH%
cd /d %~dp0
echo Starting Swordie server via Maven...
mvn exec:java -Dexec.mainClass="net.swordie.ms.Server"
