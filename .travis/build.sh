#/bin/bash
set -e

cwd=`pwd`
root=`git rev-parse --show-toplevel`

assignments=`ls -1 | grep 'Assignment-*'`
for assignment in $assignments; do
    cd $root/$assignment
    ./gradlew check clean test build
done

cd $pwd
