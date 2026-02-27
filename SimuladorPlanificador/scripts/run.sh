#!/bin/bash
# Compila archivos fuente de src/ y los ejecuta utilizando el JDK portátil en Linux/macOS.

if [ ! -d "./jdk" ]; then
    echo "JDK no encontrado. Ejecuta scripts/setup.sh primero."
    exit 1
fi

export JAVA_HOME="$(pwd)/jdk"
export PATH="$JAVA_HOME/bin:$PATH"

mkdir -p out
echo "Compilando archivos fuente..."
find src -name "*.java" > sources.txt
./jdk/bin/javac -d out @sources.txt
rm sources.txt

echo "Ejecutando Simulacion..."
./jdk/bin/java -cp out Main
