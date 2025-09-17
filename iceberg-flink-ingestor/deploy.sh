#!/bin/bash

# Production Deployment Script for Iceberg Flink Ingestor
set -e

echo "🚀 Deploying Iceberg Flink Ingestor..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Build the project
echo "📦 Building project..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful!"

# Copy JAR to Flink container
echo "📋 Deploying to Flink..."
docker cp iceberg-flink-examples/target/iceberg-flink-examples-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

echo "✅ Deployment completed!"
echo ""
echo "🎯 Usage Examples:"
echo ""
echo "1. Submit Customer Ingestion Job:"
echo "   docker exec flink-jobmanager /opt/flink/bin/flink run -d \\"
echo "     -c com.example.ingestor.GenericIngestionJob \\"
echo "     /opt/flink/usrlib/iceberg-flink-examples-1.0-SNAPSHOT.jar \\"
echo "     customers-config.json"
echo ""
echo "2. Submit Order Ingestion Job:"
echo "   docker exec flink-jobmanager /opt/flink/bin/flink run -d \\"
echo "     -c com.example.ingestor.GenericIngestionJob \\"
echo "     /opt/flink/usrlib/iceberg-flink-examples-1.0-SNAPSHOT.jar \\"
echo "     orders-config.json"
echo ""
echo "3. Submit Custom Table Job:"
echo "   docker exec flink-jobmanager /opt/flink/bin/flink run -d \\"
echo "     -c com.example.ingestor.GenericIngestionJob \\"
echo "     /opt/flink/usrlib/iceberg-flink-examples-1.0-SNAPSHOT.jar \\"
echo "     /path/to/your/config.json"
echo ""
echo "📊 Monitor jobs at: http://localhost:8081"
