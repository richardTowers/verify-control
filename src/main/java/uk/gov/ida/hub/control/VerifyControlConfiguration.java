package uk.gov.ida.hub.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import uk.gov.ida.hub.control.configuration.RedisConfiguration;

public class VerifyControlConfiguration extends Configuration {
    private final RedisConfiguration redis;

    @JsonCreator
    public VerifyControlConfiguration(@JsonProperty("redis") RedisConfiguration redis) {
        this.redis = redis;
    }

    public RedisConfiguration getRedis() {
        return redis;
    }
}
