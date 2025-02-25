#!/bin/bash

# Simple build script for the Fallout plugin

echo "Building Fallout plugin..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install Maven to build the plugin."
    exit 1
fi

# Clean and package the plugin
mvn clean package

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful! The plugin JAR is in the target directory."
    echo "Copy target/fallout-1.0-SNAPSHOT.jar to your server's plugins folder."
else
    echo "Build failed. Please check the error messages above."
    exit 1
fi 