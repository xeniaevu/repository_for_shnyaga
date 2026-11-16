@echo off
rem Compile and run (Windows)
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
  pause
  exit /b 1
)
del sources.txt
java -cp out antcolony.Main
