#/bin/bash
failOnError() {
    if [ "$?" != "0" ]; then
        exit 1
    fi
}
CWD=`pwd`
cd Assignment-01
failOnError
./gradlew check clean test build
failOnError
cd $CWD
failOnError
cd Assignment-02
failOnError
./gradlew clean clean test build
failOnError
cd $CWD
failOnError
