package com.javanut.gl.impl.telemetry;

import com.javanut.gl.api.TelemetryConfig;
import com.javanut.gl.impl.BridgeConfigStage;
import com.javanut.pronghorn.network.NetGraphBuilder;

public class TelemetryConfigImpl implements TelemetryConfig {
    private String host;
    private int port = TelemetryConfig.defaultTelemetryPort;

    public TelemetryConfigImpl(String host, int port) {
        this.port = port;
        this.host = host;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public void finalizeDeclareConnections() {
    	this.host = port>0?NetGraphBuilder.bindHost(this.host):null; //if port is positive ensure we have the right host
    }
}
