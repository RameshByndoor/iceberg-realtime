#!/bin/bash

# Complete Real-Time Analytics Pipeline Setup
set -e

echo "🚀 Setting up complete real-time analytics pipeline..."

# 1. Start all services
echo "📦 Starting all services..."
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# 2. Setup Kafka topics
echo "📋 Setting up Kafka topics..."
./scripts/setup-kafka-topics.sh

# 3. Create Iceberg tables via REST API
echo "🧊 Creating Iceberg tables..."
curl -X POST -H "Content-Type: application/json" -d '{"namespace": ["default"], "properties": {"location": "s3a://warehouse/"}}' http://localhost:8181/v1/namespaces 2>/dev/null || echo "Namespace exists"

# Create customers table
curl -X POST -H "Content-Type: application/json" -d '{
  "name": "customers",
  "location": "s3a://warehouse/default/customers/",
  "schema": {
    "type": "struct",
    "fields": [
      { "id": 1, "name": "customer_id", "required": true, "type": "long" },
      { "id": 2, "name": "name", "required": false, "type": "string" },
      { "id": 3, "name": "email", "required": false, "type": "string" },
      { "id": 4, "name": "phone", "required": false, "type": "string" },
      { "id": 5, "name": "address", "required": false, "type": "string" },
      { "id": 6, "name": "created_at", "required": false, "type": "timestamptz" },
      { "id": 7, "name": "updated_at", "required": false, "type": "timestamptz"}
    ]
  },
  "partition-spec": {"spec-id": 0, "fields": []},
  "properties": {"write.upsert.enabled": "true"}
}' http://localhost:8181/v1/namespaces/default/tables

# Create orders table
curl -X POST -H "Content-Type: application/json" -d '{
  "name": "orders",
  "location": "s3a://warehouse/default/orders/",
  "schema": {
    "type": "struct",
    "fields": [
      { "id": 1, "name": "order_id", "required": true, "type": "long" },
      { "id": 2, "name": "customer_id", "required": true, "type": "long" },
      { "id": 3, "name": "product_name", "required": false, "type": "string" },
      { "id": 4, "name": "quantity", "required": false, "type": "int" },
      { "id": 5, "name": "unit_price", "required": false, "type": "double" },
      { "id": 6, "name": "total_amount", "required": false, "type": "double" },
      { "id": 7, "name": "order_status", "required": false, "type": "string" },
      { "id": 8, "name": "order_date", "required": false, "type": "timestamptz" }
    ]
  },
  "partition-spec": {"spec-id": 0, "fields": []},
  "properties": {}
}' http://localhost:8181/v1/namespaces/default/tables

# 4. Wait for StarRocks to be ready
echo "⏳ Waiting for StarRocks to be ready..."
sleep 60

# 5. Setup StarRocks Iceberg integration
echo "🔗 Setting up StarRocks Iceberg integration..."
./scripts/setup-starrocks-catalog.sh

# 6. Build and deploy Flink jobs
echo "🔨 Building and deploying Flink jobs..."
./scripts/build-and-deploy.sh

# 7. Submit Flink jobs
echo "🚀 Submitting Flink jobs..."
docker exec flink-jobmanager /opt/flink/bin/flink run -d -c com.example.CustomerIngestionJob /opt/flink/usrlib/iceberg-flink-jobs-1.0-SNAPSHOT.jar
docker exec flink-jobmanager /opt/flink/bin/flink run -d -c com.example.OrderIngestionJob /opt/flink/usrlib/iceberg-flink-jobs-1.0-SNAPSHOT.jar

echo "✅ Setup completed!"
echo ""
echo "🌐 Access URLs:"
echo "  - Flink Web UI: http://localhost:8081"
echo "  - Kafka UI: http://localhost:8080"
echo "  - MinIO Console: http://localhost:9001 (admin/password)"
echo "  - StarRocks FE: http://localhost:8030"
echo "  - StarRocks MySQL: localhost:9030 (user: root, no password)"
echo "  - SQLPad: http://localhost:3000 (admin@example.com/admin)"
echo ""
echo "📊 To start data generation:"
echo "  cd data-generator && python3 generate_data.py --initial-customers 5 --customers-per-minute 2 --orders-per-minute 5"
echo ""
echo "🔍 To query data via StarRocks:"
echo "  docker exec starrocks-fe mysql -h starrocks-fe -P 9030 -u root -e \"SET CATALOG iceberg_catalog; USE \`default\`; SELECT * FROM customers LIMIT 5;\""
