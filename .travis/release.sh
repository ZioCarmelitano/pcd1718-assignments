#!/bin/bash
set -e

if [ "$#" != "1" ]; then
    echo "Usage: $0 <assignment>" >& 2
    exit 1
fi

cwd=`pwd`
assignment=$1
root=`git rev-parse --show-toplevel`
releaseDir=$root/release

cd $root/$assignment

# Copy documentation
mkdir -p $releaseDir/$assignment/doc
cp doc/report/report.pdf $releaseDir/$assignment/doc

# Copy sources, scripts and gradle-related files
cp -R src gradle build.gradle gradle.properties settings.gradle gradlew* $releaseDir/$assignment || :

cd $releaseDir

# Make the zip
zip -r $assignment.zip $assignment > /dev/null
rm -r $assignment

cd $cwd
