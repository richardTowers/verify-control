package uk.gov.ida.hub.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class VerifyControlConfiguration extends Configuration {
    private final String redisUrl;
    private final String samlEngineUrl;
    private final String configUrl;
    private final String samlSoapProxyUrl;

    @JsonCreator
    public VerifyControlConfiguration(
        @JsonProperty("redisUrl") String redisUrl,
        @JsonProperty("samlEngineUrl") String samlEngineUrl,
        @JsonProperty("configUrl") String configUrl,
        @JsonProperty("samlSoapProxyUrl") String samlSoapProxyUrl) {
        this.redisUrl = redisUrl;
        this.samlEngineUrl = samlEngineUrl;
        this.configUrl = configUrl;
        this.samlSoapProxyUrl = samlSoapProxyUrl;
    }

    public String getRedisUrl() {
        return redisUrl;
    }

    public String getSamlEngineUrl() {
        return samlEngineUrl;
    }

    public String getConfigUrl() {
        return configUrl;
    }

    public String getSamlSoapProxyUrl() {
        return samlSoapProxyUrl;
    }
}
