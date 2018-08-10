#!/bin/bash

if [ "$#" != 2 ]; then
    usage
    exit 1
fi

case $1 in
    -r|--room)
        service=room
        ;;
    -u|--user)
        service=user
        ;;
    -w|--web-app)
        service=webapp
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

port=$2

java -cp build/libs/Assignment-04-1.0.jar pcd.ass04.Launcher $service $port
