/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.cobblefurnies.activation;

/** Process-scoped fail-closed state for the single exact resource route. */
public final class CobbleFurniesRuntime {

    public static final String ROUTE_ID = "cobblefurnies-1.2-athena-4.0.6";
    public static final CobbleFurniesRuntime INSTANCE = new CobbleFurniesRuntime();

    private final RouteActivation route = new RouteActivation(ROUTE_ID);
    private volatile CompiledProfile profile;

    private CobbleFurniesRuntime() {
    }

    public RouteActivation route() {
        return route;
    }

    public CompiledProfile profile() {
        return profile;
    }

    public synchronized boolean activate(CompiledProfile compiled) {
        profile = compiled;
        route.activate();
        if (!route.isActive()) {
            profile = null;
            return false;
        }
        return true;
    }

    public synchronized void inactive(String detail) {
        route.inactive(detail);
        profile = null;
    }

    public synchronized void disable(String detail) {
        route.fail(detail);
        profile = null;
    }
}
