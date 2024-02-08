package io.aeolabs.sonar.ts;


import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.sonar.api.config.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusHandler implements TimeSerial {
    private final String METRIC_PREFIX = "sonarqube_";
    private final Map<String, Gauge> gauges = new HashMap<>();


    @Override
    public void addMetric(TsMetric tsMetric) {
        this.addMetic(Collections.singletonList(tsMetric));
    }

    @Override
    public void addMetic(List<TsMetric> tsMetrics) {
        tsMetrics.forEach(measure -> this.gauges.get(measure.getMetric())
                .labels(measure.getKey(), measure.getProjectName())
                .set(measure.getValue()));
    }

    @Override
    public void updateGauges(Configuration configuration) {
        CollectorRegistry.defaultRegistry.clear();

        MetricsSettings.enabledMetrics(configuration)
                .forEach(metric -> gauges.put(metric.getKey(), Gauge.build()
                                .name(METRIC_PREFIX + metric.getKey())
                                .help(metric.getDescription())
                                .labelNames("key", "name")
                                .register()
                        )
                );
    }
}
