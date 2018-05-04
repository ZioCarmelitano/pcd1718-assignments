#!/bin/bash
set -e

if [ "$#" != "1" ]; then
    echo "Usage: $0 <assignment>" >& 2
    exit 1
fi

assignment=$1
cwd=`git rev-parse --show-toplevel`
releaseDir=$cwd/release/$assignment

# Make the release dir
mkdir -p $releaseDir

cd $cwd/$assignment

# Copy documentation
mkdir -p $releaseDir/doc
cp doc/report/report.pdf $releaseDir/doc

# Copy sources, scripts and gradle-related files
cp -R src *.sh *.bat gradle build.gradle gradle.properties settings.gradle gradlew* $releaseDir

cd $cwd/release

# Make the zip
zip -r $assignment.zip $assignment > /dev/null
rm -r $assignment

cd $cwd
