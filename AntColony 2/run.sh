#!/bin/sh
# Компиляция и запуск (macOS / Linux)
cd "$(dirname "$0")" || exit 1
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name '*.java') && java -cp out antcolony.Main
