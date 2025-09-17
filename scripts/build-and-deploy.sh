#!/bin/bash

# Build the Flink jobs
echo "Building Flink jobs..."
cd flink-jobs
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Maven build failed!"
    exit 1
fi

echo "Flink jobs built successfully!"

# Copy JAR to Flink container
echo "Deploying jobs to Flink..."
docker cp target/iceberg-flink-jobs-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

echo "Jobs deployed successfully!"

# Setup Iceberg tables
# echo "Setting up Iceberg tables..."
# cd ../scripts
# pip3 install requests
# python3 setup-iceberg-tables.py

echo "Setup complete!"
echo "You can now submit jobs via the Flink Web UI at http://localhost:8081"
