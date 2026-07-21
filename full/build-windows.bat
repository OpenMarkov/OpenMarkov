@echo off
rem Compila el fat-jar de OpenMarkov y lanza la aplicacion (Windows).
rem Cualquier argumento se pasa a OpenMarkov (ficheros a abrir, -l <idioma>, ...).

cd /d "%~dp0"

call mvn install
if errorlevel 1 exit /b 1

java -jar target\full-0.3.0-SNAPSHOT-jar-with-dependencies.jar %*
