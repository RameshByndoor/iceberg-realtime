#!/bin/bash

# Setup StarRocks Iceberg External Catalog
set -e

echo "🔗 Setting up StarRocks Iceberg external catalog..."

# Wait for StarRocks to be ready
echo "⏳ Waiting for StarRocks to be ready..."
sleep 30

# Check if StarRocks is ready
until docker exec starrocks-fe mysql -h starrocks-fe -P 9030 -u root -e "SELECT 1;" >/dev/null 2>&1; do
    echo "Waiting for StarRocks to start..."
    sleep 5
done

echo "✅ StarRocks is ready!"

# Execute the StarRocks setup script
echo "📋 Executing StarRocks setup script..."
docker exec -i starrocks-fe mysql -h starrocks-fe -P 9030 -u root < scripts/setup-starrocks.sql

echo "✅ StarRocks Iceberg catalog setup completed!"
echo ""
echo "🌐 Access StarRocks via MySQL protocol:"
echo "  - Host: localhost"
echo "  - Port: 9030"
echo "  - User: root"
echo "  - Password: (empty)"
echo ""
echo "📊 Example queries:"
echo "  - SHOW CATALOGS;"
echo "  - USE CATALOG iceberg_catalog;"
echo "  - SHOW TABLES;"
echo "  - SELECT * FROM customers LIMIT 10;"
