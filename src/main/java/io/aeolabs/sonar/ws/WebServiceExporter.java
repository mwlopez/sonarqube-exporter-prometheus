package io.aeolabs.sonar.ws;

import io.aeolabs.sonar.ts.MetricsSettings;
import io.aeolabs.sonar.ts.PrometheusHandler;
import io.aeolabs.sonar.ts.TimeSerial;
import io.aeolabs.sonar.ts.TsMetric;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.server.ws.WebService;
import org.sonarqube.ws.Components;
import org.sonarqube.ws.Measures;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.components.SearchRequest;
import org.sonarqube.ws.client.measures.ComponentRequest;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WebServiceExporter implements WebService {
    private final Configuration configuration;
    private static final String API_URL = "api/exporter";
    private final TimeSerial ts;

    public WebServiceExporter(Configuration configuration) {
        this.configuration = configuration;
        this.ts = new PrometheusHandler();
    }

    @Override
    public void define(Context context) {
        //Exporter is not enabled, just return empty.
        if (!MetricsSettings.isEnabledExporter(this.configuration)) {
            return;
        }
        MetricsSettings.enabledMetrics(this.configuration);
        ts.updateGauges(this.configuration);


        NewController controller = context.createController(API_URL);
        controller.setDescription("Metrics Exporter");


        controller.createAction("metrics")
                .setHandler(((request, response) -> {
                    Set<Metric<?>> metrics = MetricsSettings.enabledMetrics(this.configuration);
                    ts.updateGauges(this.configuration);

                    if (!metrics.isEmpty()) {
                        WsClient wsClient = WsClientFactories.getLocal().newClient(request.localConnector());
                        List<Components.Component> projects = getProjects(wsClient);

                        projects.forEach(project -> {
                            Measures.ComponentWsResponse wsResponse = getMeasures(wsClient, project);
                            List<TsMetric> tsMetrics = wsResponse.getComponent()
                                    .getMeasuresList()
                                    .stream()
                                    .map(m -> TsMetric.builder().metric(m.getMetric())
                                            .key(project.getKey())
                                            .projectName(project.getName())
                                            .value(Double.parseDouble(m.getValue()))
                                            .build())
                                    .collect(Collectors.toList());
                            ts.addMetic(tsMetrics);
                        });
                    }

                    OutputStream output = response.stream()
                            .setMediaType(TextFormat.CONTENT_TYPE_004)
                            .setStatus(200)
                            .output();


                    try (OutputStreamWriter writer = new OutputStreamWriter(output)) {

                        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());

                    }
                }
                ));

        controller.done();

    }


    private Measures.ComponentWsResponse getMeasures(WsClient wsClient, Components.Component project) {

        List<String> metricKeys = MetricsSettings.enabledMetrics(this.configuration).stream()
                .map(Metric::getKey)
                .collect(Collectors.toList());

        return wsClient.measures().component(new ComponentRequest()
                .setComponent(project.getKey())
                .setMetricKeys(metricKeys));
    }

    private List<Components.Component> getProjects(WsClient wsClient) {

        return wsClient.components()
                .search(new SearchRequest()
                        .setQualifiers(Collections.singletonList(Qualifiers.PROJECT))
                        .setPs("500"))
                .getComponentsList();

    }
}
