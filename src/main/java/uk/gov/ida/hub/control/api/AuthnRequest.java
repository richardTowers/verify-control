package uk.gov.ida.hub.control.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class AuthnRequest {
    private final String samlRequest;
    private final String relayState;
    private final String principalIPAddressAsSeenByHub;

    @JsonCreator
    public AuthnRequest(
        @JsonProperty("samlRequest") String samlRequest,
        @JsonProperty("relayState") String relayState,
        @JsonProperty("principalIPAddressAsSeenByHub") String principalIPAddressAsSeenByHub) {
        this.samlRequest = samlRequest;
        this.relayState = relayState;
        this.principalIPAddressAsSeenByHub = principalIPAddressAsSeenByHub;
    }

    @NotNull
    public String getSamlRequest() {
        return samlRequest;
    }

    @Nullable
    public String getRelayState() {
        return relayState;
    }

    @NotNull
    public String getPrincipalIPAddressAsSeenByHub() {
        return principalIPAddressAsSeenByHub;
    }
}
