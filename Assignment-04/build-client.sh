#!/bin/bash
APP_NAME=ChatCLient
cd $APP_NAME
ng build --prod
cd ..
rm -r src/main/resources/webroot/*
mv $APP_NAME/dist/$APP_NAME/* src/main/resources/webroot
