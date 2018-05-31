package uk.gov.ida.hub.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class VerifyControlConfiguration extends Configuration {
    private final String redisUrl;

    @JsonCreator
    public VerifyControlConfiguration(@JsonProperty("redisUrl") String redisUrl) {
        this.redisUrl = redisUrl;
    }

    String getRedisUrl() {
        return redisUrl;
    }
}
