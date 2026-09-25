@echo off
rem Compila el fat-jar de OpenMarkov y lanza la aplicacion (Windows).
rem Cualquier argumento se pasa a OpenMarkov (ficheros a abrir, -l <idioma>, ...).

cd /d "%~dp0.."

call mvn -pl full -am -P GenerateFullJar package -DskipTests
if errorlevel 1 exit /b 1

java -jar OpenMarkov.jar %*
