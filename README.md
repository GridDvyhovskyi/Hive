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
        
# Result:
        laptop-12.garza.net	{'m1': 6600, 'm2': 2200, 'm3': 6100, 'm4': 8600, 'm5': 9800}	{'m1': 12012, 'm2': 4004, 'm3': 13054, 'm4': 15996, 'm5': 18228, 'm6': 49}
        lt-67.smith-haney.org	{'m1': 9400, 'm2': 5000, 'm3': 9600, 'm4': 900, 'm5': 1000}	{'m1': 19740, 'm2': 10200, 'm3': 19200, 'm4': 1944, 'm5': 2000, 'm6': 51}
