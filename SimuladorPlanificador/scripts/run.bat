@echo off
:: Compila y ejecuta la aplicación utilizando el JDK local en Windows.

if not exist ".\jdk\" (
    echo JDK no encontrado. Ejecuta scripts\setup.bat primero.
    exit /b 1
)

if not exist "out" mkdir out
echo Compilando archivos fuente...
dir /s /b src\*.java > sources.txt
.\jdk\bin\javac.exe -d out @sources.txt
del sources.txt

echo Ejecutando Simulacion...
.\jdk\bin\java.exe -cp out Main
