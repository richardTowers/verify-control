package uk.gov.ida.hub.control.clients;

import com.google.common.collect.ImmutableList;
import uk.gov.ida.hub.control.dtos.samlengine.SamlRequestDto;
import uk.gov.ida.hub.control.errors.ApiBadRequestException;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class SamlEngineClient {
    private final WebTarget samlEngineWebTarget;

    public SamlEngineClient(WebTarget samlEngineWebTarget) {
        this.samlEngineWebTarget = samlEngineWebTarget;
    }

    public Map<String, String> translateRpAuthnRequest(String authnRequest) {
        return samlEngineWebTarget
            .path("/saml-engine/translate-rp-authn-request")
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlMessage", authnRequest), APPLICATION_JSON_TYPE))
            .invoke(new GenericType<Map<String, String>>() {});
    }

    public SamlRequestDto generateIdpAuthnRequest(String selectedIdp) {
        return samlEngineWebTarget
            .path("/saml-engine/generate-idp-authn-request")
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf(
                "idpEntityId", selectedIdp,
                "levelsOfAssurance", ImmutableList.of("LEVEL_1") // TODO get this from config
            ), APPLICATION_JSON_TYPE))
            .invoke(SamlRequestDto.class);
    }

    public Map<String, String> translateIdpResponse(
        String sessionId, String samlResponse,
        String principalIPAddressAsSeenByHub,
        String matchingServiceEntityId
    ) throws ApiBadRequestException {
        String path = "/saml-engine/translate-idp-authn-response";
        Response response = samlEngineWebTarget
            .path(path)
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf(
                "samlResponse", samlResponse,
                "sessionId", sessionId,
                "principalIPAddressAsSeenByHub", principalIPAddressAsSeenByHub,
                "matchingServiceEntityId", matchingServiceEntityId
            ), APPLICATION_JSON_TYPE))
            .invoke();

        var responseBody = response.readEntity(new GenericType<Map<String, String>>() {{ }});

        switch (response.getStatus()) {
            case 200:
                return responseBody;
            default:
                throw new ApiBadRequestException(
                    path,
                    responseBody.get("clientMessage"),
                    responseBody.get("exceptionType")
                );
        }
    }

    public Map<String, String> translateMatchingServiceResponse(String samlResponse) throws ApiBadRequestException {
        String path = "/saml-engine/translate-attribute-query";
        Response response = samlEngineWebTarget
            .path(path)
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlResponse", samlResponse), APPLICATION_JSON_TYPE))
            .invoke();

        var responseBody = response.readEntity(new GenericType<Map<String, String>>() {{ }});

        switch (response.getStatus()) {
            case 200:
                return responseBody;
            default:
                throw new ApiBadRequestException(
                    path,
                    responseBody.get("clientMessage"),
                    responseBody.get("exceptionType")
                );
        }

    }
}
