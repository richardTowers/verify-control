package uk.gov.ida.hub.control.clients;

import uk.gov.ida.hub.control.dtos.LevelOfAssurance;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class ConfigServiceClient {
    private final WebTarget configServiceWebTarget;

    public ConfigServiceClient(WebTarget configServiceWebTarget) {
        this.configServiceWebTarget = configServiceWebTarget;
    }

    public List<String> getEnabledIdps(String issuer, LevelOfAssurance requestedLevelOfAssurance, boolean isRegistration) {
        var configTarget = isRegistration
            ? configServiceWebTarget.path("/config/idps/{entityId}/{levelOfAssurance}/enabled")
                .resolveTemplate("entityId", issuer)
                .resolveTemplate("levelOfAssurance", requestedLevelOfAssurance)
            : configServiceWebTarget.path("/config/idps/{entityId}/enabled-for-signin")
                .resolveTemplate("entityId", issuer);

        return configTarget
            .request(APPLICATION_JSON_TYPE)
            .buildGet()
            .invoke()
            .readEntity(new GenericType<List<String>>() { });
    }

    public String getMatchingServiceEntityId(String serviceEntityId) {
        return configServiceWebTarget
            .path("/config/transactions/{entityId}/matching-service-entity-id")
            .resolveTemplate("entityId", serviceEntityId)
            .request(APPLICATION_JSON_TYPE)
            .get()
            .readEntity(String.class);
    }

    public Map<String, String> getMatchingServiceConfig(String matchingServiceEntityId) {
        return configServiceWebTarget
            .path("/config/matching-services/{entityId}")
            .resolveTemplate("entityId", matchingServiceEntityId)
            .request(APPLICATION_JSON_TYPE)
            .get()
            .readEntity(new GenericType<Map<String, String>>() {});
    }

    public boolean isEidasEnabled(String issuer) {
        return configServiceWebTarget
            .path("/config/transactions/{entityId}/eidas-enabled")
            .resolveTemplate("entityId", issuer)
            .request(APPLICATION_JSON_TYPE)
            .get()
            .readEntity(boolean.class);
    }
}
