#!/usr/bin/env bash
#
# Compila el fat-jar de OpenMarkov y lanza la aplicación (macOS).
# Cualquier argumento se pasa a OpenMarkov (ficheros a abrir, -l <idioma>, ...).
#
set -e
cd "$(dirname "$0")"

# Usar un JDK 25 si JAVA_HOME no está definido
if [ -z "${JAVA_HOME:-}" ] && [ -x /usr/libexec/java_home ]; then
    JH="$(/usr/libexec/java_home -v 25 2>/dev/null)" && export JAVA_HOME="$JH"
fi

mvn install
java -jar target/full-0.3.0-SNAPSHOT-jar-with-dependencies.jar "$@"
