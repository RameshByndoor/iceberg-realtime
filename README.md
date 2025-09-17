# 🧊 Iceberg Real-time Analytics Pipeline

A complete real-time analytics pipeline built with **Apache Iceberg**, **Apache Flink**, **Kafka**, and **StarRocks**, featuring a configurable factory pattern for easy table onboarding.

## 🏗️ Architecture

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Python    │    │    Kafka    │    │    Flink    │    │   Iceberg   │
│ Data Gen    │───▶│   (MinIO)   │───▶│  (Factory   │───▶│   (S3)      │
│             │    │             │    │  Pattern)   │    │             │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                              │
                                                              ▼
                                                   ┌─────────────┐
                                                   │ StarRocks   │
                                                   │ (Analytics) │
                                                   └─────────────┘
```

## ✨ Key Features

- **🏭 Factory Pattern**: Configurable ingestion with support for append-only and upsert modes
- **⚡ Real-time Processing**: Kafka → Flink → Iceberg pipeline
- **🔍 Analytics Ready**: StarRocks integration for fast queries
- **🐳 Docker Compose**: Complete containerized setup
- **📊 Monitoring**: Kafka UI and SQLPad for data exploration
- **🔧 Multi-module**: Clean Maven project structure

## 🚀 Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 11+
- Maven 3.6+

### 1. Clone and Setup
```bash
git clone https://github.com/RameshByndoor/iceberg-realtime.git
cd iceberg-realtime
```

### 2. Start All Services
```bash
./scripts/setup-all.sh
```

### 3. Generate Test Data
```bash
cd data-generator
python3 generate_data.py --initial-customers 5 --customers-per-minute 2 --orders-per-minute 5
```

### 4. Query Data
Access SQLPad at http://localhost:3000 (admin@example.com/admin) and run:
```sql
SET CATALOG iceberg_catalog;
USE `default`;
SELECT * FROM customers LIMIT 10;
```

## 📁 Project Structure

```
iceberg-realtime/
├── 🐳 docker-compose.yml          # All services orchestration
├── 📊 data-generator/             # Python data generator
├── 🏭 iceberg-flink-ingestor/     # Main ingestion framework
│   ├── iceberg-flink-config/      # Configuration classes
│   ├── iceberg-flink-core/        # Core factory pattern
│   └── iceberg-flink-examples/    # Example configurations
├── 🧪 flink-test-jobs/            # Test jobs (Customer/Order ingestion)
├── 📜 scripts/                    # Setup and deployment scripts
└── 📖 README.md                   # This file
```

## 🏭 Factory Pattern Architecture

The ingestion framework uses a factory pattern for maximum flexibility:

### Write Modes
- **Append-only**: Simple, high-throughput ingestion
- **Upsert**: Update/insert with equality field validation

### Components
- **Factories**: Create mode-specific components
- **Mappers**: JSON to RowData conversion
- **Validators**: Configuration validation
- **Table Builders**: Iceberg sink configuration

### Example Usage
```bash
# Submit a job with custom configuration
docker exec flink-jobmanager /opt/flink/bin/flink run -d \
  -c com.example.ingestor.GenericIngestionJob \
  /opt/flink/lib/iceberg-flink-examples-1.0-SNAPSHOT.jar \
  /path/to/your/config.json
```

## 🔧 Configuration

### Table Configuration Example
```json
{
  "table": {
    "name": "customers",
    "namespace": "default",
    "write_mode": "upsert",
    "equality_fields": ["customer_id"],
    "schema": [
      {"name": "customer_id", "type": "long"},
      {"name": "name", "type": "string"},
      {"name": "email", "type": "string"}
    ]
  },
  "kafka": {
    "bootstrap_servers": "kafka:9092",
    "topic": "customers",
    "group_id": "customers-group"
  },
  "iceberg": {
    "catalog_uri": "http://iceberg-rest:8181",
    "warehouse": "s3a://warehouse"
  }
}
```

## 🌐 Service URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| **Flink Web UI** | http://localhost:8081 | - |
| **Kafka UI** | http://localhost:8080 | - |
| **MinIO Console** | http://localhost:9001 | admin/password |
| **StarRocks FE** | http://localhost:8030 | - |
| **StarRocks MySQL** | localhost:9030 | root/(no password) |
| **SQLPad** | http://localhost:3000 | admin@example.com/admin |

## 📊 Data Flow

1. **Data Generation**: Python script generates customer/order data
2. **Kafka Ingestion**: Data published to Kafka topics
3. **Flink Processing**: Factory pattern selects appropriate components
4. **Iceberg Storage**: Data written to S3-backed Iceberg tables
5. **StarRocks Analytics**: External catalog enables fast queries
6. **SQLPad Interface**: Web-based SQL querying

## 🧪 Test Jobs

The `flink-test-jobs/` directory contains simple test jobs for basic functionality:

- **CustomerIngestionJob**: Ingests customer data from Kafka to Iceberg
- **OrderIngestionJob**: Ingests order data from Kafka to Iceberg

These are standalone jobs for testing the basic pipeline before using the factory pattern framework.

### Running Test Jobs
```bash
# Build test jobs
cd flink-test-jobs
mvn clean package

# Submit customer job
docker exec flink-jobmanager /opt/flink/bin/flink run -d \
  -c com.example.CustomerIngestionJob \
  /opt/flink/lib/iceberg-flink-jobs-1.0-SNAPSHOT.jar

# Submit order job  
docker exec flink-jobmanager /opt/flink/bin/flink run -d \
  -c com.example.OrderIngestionJob \
  /opt/flink/lib/iceberg-flink-jobs-1.0-SNAPSHOT.jar
```

## 🛠️ Development

### Building the Project
```bash
# Build main framework
cd iceberg-flink-ingestor
mvn clean package

# Build test jobs
cd ../flink-test-jobs
mvn clean package
```

### Adding New Write Modes
1. Implement `IngestionFactory` interface
2. Create mode-specific mappers, validators, and table builders
3. Register factory in `FactoryRegistry`

### Custom Table Onboarding
1. Create JSON configuration file
2. Define table schema and write mode
3. Submit job with configuration

## 📈 Monitoring

- **Flink Jobs**: Monitor at http://localhost:8081
- **Kafka Topics**: View at http://localhost:8080
- **Data Queries**: Use SQLPad at http://localhost:3000
- **Storage**: Check MinIO at http://localhost:9001

## 🔍 Troubleshooting

### Common Issues
1. **Port Conflicts**: Ensure ports 8080-8081, 9000-9001, 3000, 8030, 9030 are available
2. **Memory Issues**: Increase Docker memory allocation
3. **S3 Connection**: Verify MinIO credentials and endpoint

### Logs
```bash
# View Flink logs
docker logs flink-jobmanager

# View Kafka logs
docker logs kafka

# View StarRocks logs
docker logs starrocks-fe
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Apache Iceberg for the table format
- Apache Flink for stream processing
- StarRocks for analytics capabilities
- The open-source community for inspiration

---

**Built with ❤️ for real-time analytics**