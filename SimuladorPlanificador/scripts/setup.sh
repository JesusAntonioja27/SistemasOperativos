#!/bin/bash
# Descarga de Adoptium JDK 17 (tar.gz) en entornos UNIX y lo extrae en el directorio /jdk.

if [ ! -d "./jdk" ]; then
    echo "Descargando OpenJDK 17..."
    # Descargar usando curl o wget
    if command -v curl &> /dev/null; then
        curl -L -o jdk17.tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10+7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz"
    else
        wget -c -O jdk17.tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10+7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.10_7.tar.gz"
    fi
    
    mkdir -p ./jdk
    tar -xzf jdk17.tar.gz -C ./jdk --strip-components=1
    rm jdk17.tar.gz
    echo "JDK descargado y extraído en ./jdk"
else
    echo "El directorio ./jdk ya existe."
fi

export JAVA_HOME="$(pwd)/jdk"
export PATH="$JAVA_HOME/bin:$PATH"
