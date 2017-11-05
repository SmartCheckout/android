#!/bin/bash

jarsigner -verbose -keystore my-release-key.keystore "$1" rahul_yesh

echo ""
echo ""
echo "Checking if APK is verified..."
jarsigner -verify "$1"

