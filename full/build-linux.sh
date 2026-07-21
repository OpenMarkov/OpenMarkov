#!/usr/bin/env bash
#
# Compila el fat-jar de OpenMarkov y lanza la aplicación (Linux).
# Cualquier argumento se pasa a OpenMarkov (ficheros a abrir, -l <idioma>, ...).
#
set -e
cd "$(dirname "$0")"

mvn install
java -jar target/full-0.3.0-SNAPSHOT-jar-with-dependencies.jar "$@"
