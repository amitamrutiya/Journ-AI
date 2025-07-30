#!/bin/bash

# JournAI Java Server Startup Script

echo "Starting JournAI Java Server..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Creating .env file from .env.example..."
    cp .env.example .env
    echo "Please configure your environment variables in .env file"
fi

# Load environment variables
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Build the application
echo "Building application..."
./mvnw clean package -DskipTests

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful! Starting server..."
    java -jar target/server-0.0.1-SNAPSHOT.jar
else
    echo "Build failed!"
    exit 1
fi
