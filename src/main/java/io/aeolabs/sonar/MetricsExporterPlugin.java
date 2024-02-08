package io.aeolabs.sonar;

import io.aeolabs.sonar.ts.MetricsSettings;
import io.aeolabs.sonar.ws.WebServiceExporter;
import org.sonar.api.Plugin;

/**
 * Metric Exporter Plugin.
 *
 * @author Marcelo Lopez <mwlopez@gmail.com>
 */
public class MetricsExporterPlugin implements Plugin {

    @Override
    public void define(Context context) {

        context.addExtension(WebServiceExporter.class);
        context.addExtensions(MetricsSettings.definitions());

    }
}
