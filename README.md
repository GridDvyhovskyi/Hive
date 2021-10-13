# Hive Task


There are several additional steps in order to resolve the task:
1. Tow remote tables (for v1 and v2 data sets) sgould be created within hive/metastore:
------------------------------------------------
        CREATE EXTERNAL TABLE IF NOT EXISTS metrics_v1 (
          script_version Float,
          time_point Timestamp,
          hostname String,
          uuid String,
          base_metric Double,
          metrics String)
        COMMENT 'initial_metrics'
        ROW FORMAT DELIMITED 
        FIELDS TERMINATED BY '\t'
        STORED AS TEXTFILE
        LOCATION '/tmp/hive/ubuntu/v1/sample';
------------------------------------------------
        CREATE EXTERNAL TABLE IF NOT EXISTS metrics_v2 (
          script_version Float,
          time_point Timestamp,
          hostname String,
          uuid String,
          base_metric Double,
          metrics String)
        COMMENT 'current_metrics'
        ROW FORMAT DELIMITED 
        FIELDS TERMINATED BY '\t'
        STORED AS TEXTFILE
        LOCATION '/tmp/hive/ubuntu/v2/sample';

2. The myudf.jar should be uploaded to aws instance and to hdfs eventually
3. In the hive CLI:
- ADD JAR /home/ubuntu/myudf.jar;
- CREATE TEMPORARY function calculate_regression AS 'com.grid.MetricsExtractor';
4. Run SELECT statement:
------------------------------------------------
        SELECT reg.hostname, reg.v1, reg.v2 FROM (SELECT metrics_v1.hostname, metrics_v1.metrics as v1, metrics_v2.metrics as v2 FROM metrics_v1 
        INNER JOIN metrics_v2 ON metrics_v1.hostname=metrics_v2.hostname AND metrics_v1.uuid=metrics_v2.uuid) reg 
        WHERE calculate_regression(reg.v1, reg.v2) IS NOT NULL;
