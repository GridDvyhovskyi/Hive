package com.grid;

import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.io.Text;

import java.util.*;

public final class MetricsExtractor extends UDF {

    private Text result = new Text();

    public Text evaluate(Text initial, Text current) {
        if(initial == null || current == null) {
            return null;
        }
        Map<String, Integer> initialMetricsMap = new TreeMap<>();
        String[] initialMetrics = initial.toString().replaceAll("[{}'\\s+]", "").split(",");
        for(String metric: initialMetrics){
            String[] splitMetric = metric.split(":");
            initialMetricsMap.put(splitMetric[0], Integer.valueOf(splitMetric[1]));
        }
        Map<String, Integer> currentMetricsMap = new TreeMap<>();
        String[] currentMetrics = current.toString().replaceAll("[{}'\\s+]", "").split(",");
        for(String metric: currentMetrics){
            String[] splitMetric = metric.split(":");
            currentMetricsMap.put(splitMetric[0], Integer.valueOf(splitMetric[1]));
        }
        Map<String, Integer> regressionMetricsMap = new TreeMap<>();

        for(String metricID: initialMetricsMap.keySet()){
            if((currentMetricsMap.get(metricID)-initialMetricsMap.get(metricID))%initialMetricsMap.get(metricID) >
                    initialMetricsMap.get(metricID)*0.1) {
                regressionMetricsMap.put(metricID, currentMetricsMap.get(metricID) - initialMetricsMap.get(metricID));
            }
        }
        result.set(regressionMetricsMap.toString());
        return regressionMetricsMap.keySet().isEmpty() ? null : result;
    }

}
