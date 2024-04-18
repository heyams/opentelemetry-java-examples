package io.opentelemetry.example.azure.resource.detector;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.contrib.azure.resource.AzureAppServiceResourceProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.TimeUnit;

public class AzureResourceDetectorExample {
  private static final String INSTRUMENTATION_SCOPE_NAME =
      AzureResourceDetectorExample.class.getName();

  public static void main(String[] args) {
    // Get the autoconfigured OpenTelemetry SDK
    OpenTelemetrySdk openTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    // Shows the resource attributes detected from the environment variables
    // and system properties.
    System.out.println("Detecting resource: Environment");
    Resource autoResource = ResourceConfiguration.createEnvironmentResource();
    System.out.println(autoResource.getAttributes() + "\n");

    // Shows the resource attributes detected by the Azure App Service Resource Provider
    System.out.println("Detecting resource: hardcoded");
    AzureAppServiceResourceProvider resourceProvider = new AzureAppServiceResourceProvider();
    System.out.println(resourceProvider.getAttributes() + "\n");

    // Shows the attributes attached to the Resource that was set for TracerProvider
    // via the autoconfiguration SPI.
    System.out.println("Detecting resource: Azure resources");
    Span span =
        openTelemetrySdk
            .getTracer(INSTRUMENTATION_SCOPE_NAME)
            .spanBuilder("azure-resource-providers-detector")
            .startSpan();
    try (Scope ignored = span.makeCurrent()) {
      // Simulate work: this could be simulating a network request or an expensive disk operation
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      span.end();
    }
    // Flush all buffered traces
    CompletableResultCode completableResultCode =
        openTelemetrySdk.getSdkTracerProvider().shutdown();
    // wait till export finishes
    completableResultCode.join(10000, TimeUnit.MILLISECONDS);
  }
}
