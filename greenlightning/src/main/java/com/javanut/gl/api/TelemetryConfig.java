package com.javanut.gl.api;

public interface TelemetryConfig {
    int defaultTelemetryPort = 8098;

    String getHost();

    int getPort();
}
