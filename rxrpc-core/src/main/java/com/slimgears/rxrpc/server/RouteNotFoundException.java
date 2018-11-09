package com.slimgears.rxrpc.server;

public class RouteNotFoundException extends RuntimeException {
    private final String route;

    public RouteNotFoundException(String route) {
        super("Route " + route + " was not found");
        this.route = route;
    }

    public String getRoute() {
        return route;
    }
}
