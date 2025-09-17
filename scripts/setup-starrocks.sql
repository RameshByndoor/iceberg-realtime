-- StarRocks Setup for Iceberg Integration
-- Based on official StarRocks documentation: https://docs.starrocks.io/docs/quick_start/iceberg/

-- Create Iceberg external catalog in StarRocks
CREATE EXTERNAL CATALOG 'iceberg_catalog'
COMMENT "External catalog to Apache Iceberg on MinIO"
PROPERTIES (
    "type" = "iceberg",
    "iceberg.catalog.type" = "rest",
    "iceberg.catalog.uri" = "http://iceberg-rest:8181",
    "iceberg.catalog.warehouse" = "warehouse",
    "aws.s3.access_key" = "admin",
    "aws.s3.secret_key" = "password",
    "aws.s3.endpoint" = "http://minio:9000",
    "aws.s3.enable_path_style_access" = "true",
    "client.factory" = "com.starrocks.connector.iceberg.IcebergAwsClientFactory"
);

-- Show available catalogs
SHOW CATALOGS\G

-- Switch to Iceberg catalog to see tables
SET CATALOG iceberg_catalog;

-- Show tables in the Iceberg catalog
SHOW TABLES;

-- Switch back to default catalog
SET CATALOG default_catalog;
