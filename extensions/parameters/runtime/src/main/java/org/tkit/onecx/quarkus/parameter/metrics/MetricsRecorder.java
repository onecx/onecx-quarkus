package org.tkit.onecx.quarkus.parameter.metrics;

public interface MetricsRecorder {

    String TAG_NAME = "name";
    String TAG_STATUS = "status";
    String TAG_TYPE = "type";
    String TAG_TENANT = "tenant";

    String TYPE_START_UP = "start-up";
    String TYPE_SCHEDULER = "scheduler";
    String TYPE_NO_CACHE = "no-cache";
    String TYPE_CACHE = "cache";
    String STATUS_UNDEFINED = "UNDEFINED";

    void increase(String name);

    void update(String tenant, String type, String status);

    void history(String tenant, String status);
}
