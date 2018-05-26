package uk.gov.ida.hub.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RedisConfiguration {
    private final String host;
    private final int port;

    @JsonCreator
    public RedisConfiguration(@JsonProperty("host") String host, @JsonProperty("port") int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
