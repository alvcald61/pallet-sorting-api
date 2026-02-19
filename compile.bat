@echo off
echo ========================================
echo Compilando con Java 17
echo ========================================
set JAVA_HOME=C:\Program Files\Java\jdk-17.0.2
set PATH=%JAVA_HOME%\bin;%PATH%
mvn clean compile -DskipTests
pause
