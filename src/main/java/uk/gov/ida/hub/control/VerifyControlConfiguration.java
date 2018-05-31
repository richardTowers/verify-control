package uk.gov.ida.hub.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class VerifyControlConfiguration extends Configuration {
    private final String redisUrl;
    private String samlEngineUrl;

    @JsonCreator
    public VerifyControlConfiguration(
        @JsonProperty("redisUrl") String redisUrl,
        @JsonProperty("samlEngineUrl") String samlEngineUrl
    ) {
        this.redisUrl = redisUrl;
        this.samlEngineUrl = samlEngineUrl;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public String getSamlEngineUrl() {
        return samlEngineUrl;
    }
}
