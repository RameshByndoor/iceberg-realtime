# Iceberg Flink Ingestor

Production-ready, configurable real-time data ingestion from Kafka to Iceberg tables using Apache Flink.

## 🏗️ Architecture

```
Kafka → Generic Flink Job → Iceberg Tables (S3/MinIO)
```

## 📁 Project Structure

```
iceberg-flink-ingestor/
├── iceberg-flink-core/          # Core generic ingestion logic
├── iceberg-flink-config/        # Configuration management
├── iceberg-flink-examples/      # Example configurations and usage
└── pom.xml                      # Parent POM
```

## 🚀 Quick Start

### 1. Build the Project

```bash
mvn clean package
```

### 2. Run with Configuration File

```bash
# Using local config file
java -cp iceberg-flink-examples/target/iceberg-flink-examples-1.0-SNAPSHOT.jar \
  com.example.ingestor.GenericIngestionJob \
  iceberg-flink-examples/src/main/resources/customers-config.json

# Using classpath resource
java -cp iceberg-flink-examples/target/iceberg-flink-examples-1.0-SNAPSHOT.jar \
  com.example.ingestor.GenericIngestionJob \
  customers-config.json
```

### 3. Submit to Flink Cluster

```bash
# Submit to Flink cluster
flink run -c com.example.ingestor.GenericIngestionJob \
  iceberg-flink-examples/target/iceberg-flink-examples-1.0-SNAPSHOT.jar \
  /path/to/your/config.json
```

## ⚙️ Configuration

### Configuration File Format

The ingestion job is configured via JSON files. Here's the structure:

```json
{
  "table": {
    "name": "table_name",
    "namespace": "default",
    "write_mode": "append|upsert",
    "equality_fields": ["field1", "field2"],
    "schema": [
      {
        "name": "field_name",
        "type": "long|int|double|string|boolean|timestamp",
        "required": true|false,
        "mapping": "json_field_name"
      }
    ]
  },
  "kafka": {
    "bootstrap_servers": "kafka:29092",
    "topic": "topic_name",
    "group_id": "consumer_group",
    "starting_offset": "latest|earliest"
  },
  "iceberg": {
    "catalog_uri": "http://iceberg-rest:8181",
    "warehouse": "s3a://warehouse",
    "io_impl": "org.apache.iceberg.aws.s3.S3FileIO"
  },
  "aws": {
    "region": "us-east-1",
    "access_key_id": "access_key",
    "secret_access_key": "secret_key",
    "endpoint": "http://minio:9000",
    "path_style_access": true
  }
}
```

### Example Configurations

- **customers-config.json**: Upsert mode with customer_id as equality field
- **orders-config.json**: Append mode for order transactions

## 🔧 Adding New Tables

To onboard a new table, simply create a new configuration file:

1. **Create config file** (e.g., `new-table-config.json`)
2. **Define table schema** with field mappings
3. **Configure Kafka topic** and consumer group
4. **Set write mode** (append for immutable data, upsert for mutable data)
5. **Run the job** with the new config

### Example: Adding a Products Table

```json
{
  "table": {
    "name": "products",
    "namespace": "default",
    "write_mode": "upsert",
    "equality_fields": ["product_id"],
    "schema": [
      {
        "name": "product_id",
        "type": "long",
        "required": true,
        "mapping": "product_id"
      },
      {
        "name": "product_name",
        "type": "string",
        "required": false,
        "mapping": "name"
      },
      {
        "name": "price",
        "type": "double",
        "required": false,
        "mapping": "price"
      }
    ]
  },
  "kafka": {
    "bootstrap_servers": "kafka:29092",
    "topic": "products",
    "group_id": "products-ingestion-group",
    "starting_offset": "latest"
  },
  "iceberg": {
    "catalog_uri": "http://iceberg-rest:8181",
    "warehouse": "s3a://warehouse",
    "io_impl": "org.apache.iceberg.aws.s3.S3FileIO"
  },
  "aws": {
    "region": "us-east-1",
    "access_key_id": "admin",
    "secret_access_key": "password",
    "endpoint": "http://minio:9000",
    "path_style_access": true
  }
}
```

## 🎯 Features

- **Generic Configuration**: JSON-based configuration for any table schema
- **Flexible Field Mapping**: Map JSON fields to Iceberg columns
- **Write Modes**: Support for both append and upsert operations
- **Type Safety**: Automatic type conversion from JSON to Iceberg types
- **Production Ready**: Built for scalability and reliability
- **Easy Onboarding**: Add new tables with minimal effort

## 🔄 Migration from Local Jobs

This production project is designed to replace the local `flink-jobs` while maintaining compatibility:

- **Same Dependencies**: Uses identical Flink and Iceberg versions
- **Same Infrastructure**: Works with existing Docker setup
- **Configuration Driven**: No code changes needed for new tables
- **Backward Compatible**: Can run alongside existing jobs during migration

## 📊 Monitoring

- **Flink Web UI**: Monitor job status and metrics
- **Kafka UI**: Monitor topic consumption
- **Iceberg REST**: Monitor table metadata and snapshots
- **MinIO Console**: Monitor S3 storage usage

## 🛠️ Development

### Building Individual Modules

```bash
# Build core module
cd iceberg-flink-core && mvn clean package

# Build config module
cd iceberg-flink-config && mvn clean package

# Build examples module
cd iceberg-flink-examples && mvn clean package
```

### Testing Configuration

```bash
# Validate configuration
java -cp iceberg-flink-config/target/iceberg-flink-config-1.0-SNAPSHOT.jar \
  com.example.config.ConfigLoader \
  /path/to/config.json
```
