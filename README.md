# Real-time Data Pipeline with Flink, Iceberg, and StarRocks

A complete real-time data pipeline using Apache Flink 1.20, Apache Iceberg, StarRocks, and Kafka for streaming analytics.

## 🏗️ Architecture

```
Kafka → Flink Jobs → Iceberg Tables (S3/MinIO) → StarRocks → SQLPad
```

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 11+
- Maven 3.6+
- Python 3.8+

### One-Command Setup
```bash
./scripts/setup-all.sh
```

This single command will:
1. Start all Docker services
2. Create Kafka topics
3. Create Iceberg tables via REST API
4. Setup StarRocks with Iceberg external catalog
5. Build and deploy Flink jobs
6. Submit both Customer and Order ingestion jobs

### Generate Test Data
```bash
cd data-generator
python3 generate_data.py --initial-customers 5 --customers-per-minute 2 --orders-per-minute 5
```

## 📁 Available Scripts

| Script | Purpose |
|--------|---------|
| `setup-all.sh` | Complete pipeline setup (recommended) |
| `setup-kafka-topics.sh` | Create Kafka topics |
| `setup-starrocks-catalog.sh` | Configure StarRocks Iceberg integration |
| `build-and-deploy.sh` | Build and deploy Flink jobs |

## 🌐 Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| Flink Web UI | http://localhost:8081 | - |
| Kafka UI | http://localhost:8080 | - |
| MinIO Console | http://localhost:9001 | admin/password |
| StarRocks FE | http://localhost:8030 | - |
| StarRocks MySQL | localhost:9030 | root/(no password) |
| SQLPad | http://localhost:3000 | admin@example.com/admin |

## 📊 Components

### Core Services
- **Apache Flink 1.20**: Stream processing engine
- **Apache Kafka**: Message broker
- **Apache Iceberg**: Table format for data lake
- **MinIO**: S3-compatible object storage
- **StarRocks**: Real-time analytics database
- **SQLPad**: Web-based SQL query interface

### Data Flow
1. **Data Generation**: Python script generates customer and order data
2. **Kafka Streaming**: Data flows through Kafka topics
3. **Flink Processing**: Flink jobs consume from Kafka and write to Iceberg
4. **Iceberg Storage**: Data stored in S3/MinIO with ACID properties
5. **StarRocks Query**: StarRocks reads from Iceberg for analytics
6. **SQLPad Interface**: Web UI for querying and visualization

## 🔧 Configuration

### Flink Jobs
- **CustomerIngestionJob**: Processes customer data with upsert capability
- **OrderIngestionJob**: Processes order data (append-only)

### Iceberg Tables
- **customers**: Customer master data with upsert support
- **orders**: Order transaction data

### StarRocks Integration
- External catalog configuration for Iceberg
- MySQL-compatible interface (port 9030)
- Direct access to Iceberg tables via external catalog
- SQLPad connects to StarRocks via MySQL protocol

## 📝 SQL Examples

### Query Iceberg data via StarRocks
```sql
-- Connect to StarRocks via MySQL protocol
-- Host: localhost, Port: 9030, User: root

-- Show available catalogs
SHOW CATALOGS;

-- Switch to Iceberg catalog
USE CATALOG iceberg_catalog;

-- Show tables in Iceberg catalog
SHOW TABLES;

-- Query customers table
SELECT * FROM customers LIMIT 10;

-- Query orders table
SELECT * FROM orders LIMIT 10;

-- Customer order summary
SELECT 
    c.name,
    COUNT(o.order_id) as order_count,
    SUM(o.total_amount) as total_spent
FROM customers c
JOIN orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name
ORDER BY total_spent DESC;
```

## 🛠️ Development

### Project Structure
```
├── flink-jobs/           # Flink streaming jobs
├── data-generator/       # Python data generator
├── scripts/             # Setup and utility scripts
├── docker-compose.yml   # Service orchestration
└── README.md           # This file
```

### Adding New Jobs
1. Create new Java class extending the base pattern
2. Update `pom.xml` if new dependencies needed
3. Build and deploy: `./scripts/build-and-deploy.sh`
4. Submit job via Flink Web UI or CLI

### Monitoring
- **Flink Web UI**: Job status, metrics, and logs
- **Kafka UI**: Topic monitoring and message inspection
- **MinIO Console**: Storage usage and file management
- **StarRocks FE**: Query performance and system status

## 🔍 Troubleshooting

### Common Issues
1. **AWS Region Error**: Ensure `aws.region=us-east-1` is set in all Flink configurations
2. **Table Not Found**: Recreate tables using `./scripts/setup-iceberg.sql`
3. **StarRocks Connection**: Wait for StarRocks to fully initialize (2-3 minutes)
4. **Data Not Appearing**: Check Flink job status and Kafka topic messages

### Logs
```bash
# Flink logs
docker logs flink-jobmanager
docker logs flink-taskmanager

# StarRocks logs
docker logs starrocks-fe
docker logs starrocks-be

# Kafka logs
docker logs kafka
```

## 📈 Performance Tuning

### Flink
- Adjust `taskmanager.numberOfTaskSlots` based on CPU cores
- Tune `parallelism.default` for job parallelism
- Configure checkpoint intervals for fault tolerance

### StarRocks
- Optimize query performance with proper indexing
- Use materialized views for common aggregations
- Configure memory settings based on available resources

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.