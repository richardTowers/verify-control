package uk.gov.ida.hub.control.clients;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import javax.ws.rs.client.WebTarget;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class SamlSoapProxyClient {
    private final WebTarget samlSoapProxyWebTarget;

    public SamlSoapProxyClient(WebTarget samlSoapProxyWebTarget) {
        this.samlSoapProxyWebTarget = samlSoapProxyWebTarget;
    }

    public void makeMatchingServiceRequest(
        String sessionId,
        String requestId,
        String encryptedMatchingDatasetAssertion,
        String authnStatementAssertion,
        String matchingServiceEntityId,
        String levelOfAssurance,
        String persistentId,
        String attributeQueryUri,
        String issuer,
        String onboarding) {
        var samlSoapProxyRequest = ImmutableMap.builder()
            .put("requestId", requestId)
            .put("encryptedMatchingDatasetAssertion", encryptedMatchingDatasetAssertion)
            .put("authnStatementAssertion", authnStatementAssertion)
            .put("authnRequestIssuerEntityId", issuer)
            .put("assertionConsumerServiceUri", "https://todo_get_this_from_config") // TODO get this from config
            .put("matchingServiceEntityId", matchingServiceEntityId)
            .put("matchingServiceRequestTimeout", DateTime.now().plusMinutes(5)) // TODO don't hardcode this
            .put("levelOfAssurance", levelOfAssurance)
            .put("persistentId", persistentId)
            .put("assertionExpiry", DateTime.now().plusMinutes(5)) // TODO don't hardcode this
            .put("attributeQueryUri", attributeQueryUri)
            .put("onboarding", onboarding)
            .build();

        int samlSoapProxyResponseStatus = samlSoapProxyWebTarget
            .path("/matching-service-request-sender")
            .queryParam("sessionId", sessionId)
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(samlSoapProxyRequest, APPLICATION_JSON_TYPE))
            .invoke()
            .getStatus();

        if (samlSoapProxyResponseStatus != 200) {
            throw new RuntimeException("TODO: better exception"); // TODO better exception
        }
    }

    public void makeCycle3MatchingServiceRequest(String sessionId, String id, String issuer) {
        var samlSoapProxyRequest = ImmutableMap.builder()
            .put("id", id)
            .put("issuer", issuer)
            .put("samlRequest", "TODO") // TODO
            .put("matchingServiceUri", "TODO") // TODO
            .put("assertionConsumerServiceUri", "https://todo_get_this_from_config") // TODO get this from config
            .put("attributeQueryClientTimeOut", DateTime.now().plusMinutes(5)) // TODO get this from config
            .put("onboarding", "TODO") // TODO
            .build();

        int samlSoapProxyResponseStatus = samlSoapProxyWebTarget
            .path("/matching-service-request-sender")
            .queryParam("sessionId", sessionId)
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(samlSoapProxyRequest, APPLICATION_JSON_TYPE))
            .invoke()
            .getStatus();

        if (samlSoapProxyResponseStatus != 200) {
            throw new RuntimeException("TODO: better exception"); // TODO better exception
        }
    }
}
