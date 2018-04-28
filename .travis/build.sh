#/bin/bash
set -e
CWD=`git rev-parse --show-toplevel`
assignments=`ls -1 | grep 'Assignment-*'`
for assignment in $assignments; do
    cd $CWD/$assignment
    ./gradlew check clean test build
done
cd $CWD
