package io.aeolabs.sonar.ts;

import org.sonar.api.PropertyType;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.server.ServerSide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@ServerSide
public class MetricsSettings {
    public static final Set<Metric<?>> SUPPORTED_METRICS = new HashSet<>();
    public static final String PREFIX = "sonar.metrics.export.";
    private final Configuration config;
    public static final String ENABLED = "sonar.metrics.export.enabled";
    public static final String TSNAME = "sonar.metrics.export.ts";
    public static final String CATEGORY = "Metrics Exporter";

    static {
        SUPPORTED_METRICS.add(CoreMetrics.BUGS);
        SUPPORTED_METRICS.add(CoreMetrics.CODE_SMELLS);
        SUPPORTED_METRICS.add(CoreMetrics.COVERAGE);
        SUPPORTED_METRICS.add(CoreMetrics.VULNERABILITIES);
        SUPPORTED_METRICS.add(CoreMetrics.RELIABILITY_RATING);
        SUPPORTED_METRICS.add(CoreMetrics.DUPLICATED_LINES);
        SUPPORTED_METRICS.add(CoreMetrics.TECHNICAL_DEBT);
        SUPPORTED_METRICS.add(CoreMetrics.PROJECTS);
        SUPPORTED_METRICS.add(CoreMetrics.COGNITIVE_COMPLEXITY);
        SUPPORTED_METRICS.add(CoreMetrics.CRITICAL_VIOLATIONS);
    }

    public MetricsSettings(Configuration cnf) {
        this.config = cnf;
    }


    public static List<PropertyDefinition> definitions() {
        List<PropertyDefinition> properties = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(1);
        properties.add(PropertyDefinition.builder(ENABLED)
                .name("Enable Metric Exporter")
                .description("Setting this property to true enables metrics exporter")
                .category(CATEGORY)
                .type(PropertyType.BOOLEAN)
                .defaultValue(String.valueOf(true))
                .index(index.getAndIncrement())
                .build());

        properties.add(PropertyDefinition.builder(TSNAME)
                .name("Timeserial")
                .description("Setting this property for time serial type.")
                .category(CATEGORY)
                .type(PropertyType.SINGLE_SELECT_LIST)
                .defaultValue("prometheus")
                .options("prometheus")
                .index(index.getAndIncrement())
                .build());

        List<PropertyDefinition> metrics = SUPPORTED_METRICS.stream()
                .map(metric -> PropertyDefinition.builder(PREFIX + metric.getKey())
                        .name(String.format("Export \"%s\" as Metric.", metric.getName()))
                        .description(Objects.requireNonNull(metric.getDescription()))
                        .category(CATEGORY)
                        .type(PropertyType.BOOLEAN)
                        .defaultValue(Boolean.TRUE.toString())
                        .index(index.getAndIncrement())
                        .build())
                .collect(Collectors.toList());

        properties.addAll(metrics);

        return properties;
    }


    public static Set<Metric<?>> enabledMetrics(Configuration configuration) {
        Map<Boolean, List<Metric<?>>> enabledMetrics = SUPPORTED_METRICS.stream()
                .collect(Collectors.groupingBy(metric -> configuration.getBoolean(PREFIX + metric.getKey())
                        .orElse(false)));

        Set<Metric<?>> metrics = new HashSet<>();

        if (nonNull(enabledMetrics.get(true))) {
            metrics.addAll(enabledMetrics.get(true));
        }

        return metrics;
    }

    public static Boolean isEnabledExporter(Configuration configuration) {
        return  configuration.getBoolean(ENABLED).get();
    }
}
