@echo off
:: Descarga de Adoptium JDK 17 (zip) para entornos Windows y lo extrae en el directorio jdk.

if not exist ".\jdk\" (
    echo Descargando OpenJDK 17...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.10+7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.10_7.zip' -OutFile 'jdk17.zip'"
    
    echo Extrayendo JDK...
    powershell -Command "Expand-Archive -Path 'jdk17.zip' -DestinationPath '.\jdk_temp'"
    
    :: Mover contenido para que no quede anidado
    move .\jdk_temp\jdk-17* .\jdk >nul
    rmdir /S /Q .\jdk_temp
    del .\jdk17.zip
    
    echo JDK descargado y extraido en .\jdk
) else (
    echo El directorio .\jdk ya existe.
)
