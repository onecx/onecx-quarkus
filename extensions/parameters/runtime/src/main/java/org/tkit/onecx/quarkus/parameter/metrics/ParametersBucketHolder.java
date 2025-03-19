package org.tkit.onecx.quarkus.parameter;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class ParametersBucketHolder {

    private final ParametersBucketItem bucket;

    public ParametersBucketHolder() {
        bucket = new ParametersBucketItem();
        bucket.setStart(now());
    }

    /**
     * Add a new request to the bucket, this will increase the count of requests
     * for the parameter.
     *
     * @param name the name of the parameter requested
     * @param clazz the class/type of the parameter requested
     * @param defaultValue the default value of the parameter requested
     */
    public void addParameterRequest(String name, Class<?> clazz, String defaultValue, String currentValue) {
        bucket.getParameters()
                .computeIfAbsent(name, s -> new ParametersBucketItem.ParameterInfoItem(clazz, defaultValue, currentValue))
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
