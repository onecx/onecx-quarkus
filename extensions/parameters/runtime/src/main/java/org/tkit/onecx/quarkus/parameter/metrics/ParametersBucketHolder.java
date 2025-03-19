package org.tkit.onecx.quarkus.parameter.metrics;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class ParametersBucketHolder {

    private final ParametersBucketItem bucket;

    public ParametersBucketHolder() {
        bucket = new ParametersBucketItem();
        bucket.setStart(now());
    }

    public void addParameterRequest(String name, String defaultValue, Object currentValue, String description) {
        bucket.getParameters()
                .computeIfAbsent(name, s -> new ParametersBucketItem.ParameterInfoItem(defaultValue, currentValue, description))
                .getCount().incrementAndGet();
    }

    public void setInstanceId(String instanceId) {
        bucket.setInstanceId(instanceId);
    }

    public void end() {
        bucket.setEnd(now());
    }

    public ParametersBucketItem getBucket() {
        return bucket;
    }

    static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneId.of("UTC"));
    }

}
