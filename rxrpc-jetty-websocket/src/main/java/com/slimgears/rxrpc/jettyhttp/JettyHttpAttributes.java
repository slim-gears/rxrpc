package com.slimgears.rxrpc.jettyhttp;

import java.time.Duration;

public class JettyHttpAttributes {
    public static final String ClientIdAttribute = "X-RPC-CLIENT-ID";
    public static final Duration ClientPollingPeriod = Duration.ofMillis(1000);
    public static final Duration ClientPollingRetryInitialDelay = Duration.ofMillis(1000);
    public static final int ClientPollingRetryCount = 10;
    public static final Duration ServerKeepAliveTimeout = Duration.ofSeconds(5);
}
