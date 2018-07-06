package uk.gov.ida.hub.control.data.samlengine;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SamlRequestDto {
    private final String samlRequest;
    private final String ssoUri;

    @JsonCreator
    public SamlRequestDto(
        @JsonProperty(value = "samlRequest", required = true) String samlRequest,
        @JsonProperty(value = "ssoUri", required = true) String ssoUri) {
        this.samlRequest = samlRequest;
        this.ssoUri = ssoUri;
    }

    public String getSamlRequest() {
        return samlRequest;
    }

    public String getSsoUri() {
        return ssoUri;
    }
}
