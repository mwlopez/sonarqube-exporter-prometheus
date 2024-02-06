package io.aeolabs.sonar.ts;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TsMetric {
    private final String metric;
    private final String key;
    private final String projectName;
    private final Double value;
}
