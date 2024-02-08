package io.aeolabs.sonar.ts;


import org.sonar.api.config.Configuration;

import java.util.List;

public interface TimeSerial {
    public void addMetric(TsMetric tsMetric);
    public void addMetic(List<TsMetric> tsMetrics);
    public void updateGauges(Configuration configuration);

}
