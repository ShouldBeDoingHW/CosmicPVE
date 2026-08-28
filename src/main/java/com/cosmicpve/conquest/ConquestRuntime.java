package com.cosmicpve.conquest;

public final class ConquestRuntime {
    private static final ConquestEventService EVENTS = new ConquestEventService(new ConquestRepository());
    private ConquestRuntime() {}
    public static ConquestEventService events() { return EVENTS; }
}
