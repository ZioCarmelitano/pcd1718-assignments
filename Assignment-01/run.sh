#!/bin/bash
if [ "$#" -ne 1 ]; then
    echo "Main class required." >&2
    exit 1
fi
./gradlew exec -PmainClass=$1
