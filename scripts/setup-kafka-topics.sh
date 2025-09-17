#!/bin/bash

# Wait for Kafka to be ready
echo "Waiting for Kafka to be ready..."
sleep 30

# Create Kafka topics
echo "Creating Kafka topics..."

docker exec kafka kafka-topics --create \
  --topic customers \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

docker exec kafka kafka-topics --create \
  --topic orders \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

echo "Kafka topics created successfully!"

# List topics to verify
echo "Current topics:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
