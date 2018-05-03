#!/bin/bash

usage() {
    echo "Usage: $0 -d|--max-depth <max_depth> -e|--exercise <exercise number> -p|--path <path> -r|--regex <regex>" >& 2
    echo "-d|--max-depth <max_depth> the max depth of the search" >& 2
    echo "-e|--exercise <exercise_number> the exercise number to run" >& 2
    echo "-p|--path <base_path> the base path from where to start the search" >& 2
    echo "-r|--regex <regex> the regular expression to search" >& 2
}

while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -d|--max-depth)
            MAXDEPTH="$2"
            shift # past argument
            shift # past value
            ;;        -e|--exercise)
            EXERCISE="$2"
            shift # past argument
            shift # past value
            ;;
        -p|--path)
            BASEPATH="$2"
            shift # past argument
            shift # past value
            ;;
        -r|--regex)
            REGEX="$2"
            shift # past argument
            shift # past value
            ;;
        -*|--*)
            echo "Invalid option: \"$1\"" >& 2
            usage
            exit 1
            ;;
        *)    # unknown option
            usage
            exit 1
            ;;
    esac
done

if [ -z ${MAXDEPTH+x} ]; then
    echo "option -d|--max-depth <max_depth> required" >& 2
    usage
    exit 1
fi
if [ -z ${EXERCISE+x} ]; then
    echo "option -e|--exercise <exercise_number> required" >& 2
    usage
    exit 1
fi
if [ -z ${BASEPATH+x} ]; then
    echo "option -p|--path <base_path> required" >& 2
    usage
    exit 1
fi
if [ -z ${REGEX+x} ]; then
    echo "option -r|--regex <regex> required" >& 2
    usage
    exit 1
fi

java -cp build/libs/Assignment-02-1.0.jar pcd.ass02.$EXERCISE.Launcher $BASEPATH $REGEX $MAXDEPTH
