#/bin/bash
set -e
CWD=`pwd`
cd $CWD/Assignment-01
./gradlew check clean test build
cd $CWD/Assignment-02
./gradlew clean clean test build
cd $CWD
