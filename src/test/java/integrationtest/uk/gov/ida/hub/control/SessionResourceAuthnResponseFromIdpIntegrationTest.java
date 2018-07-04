package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class SessionResourceAuthnResponseFromIdpIntegrationTest extends BaseVerifyControlIntegrationTest {
    @Ignore
    @Test
    public void responsePostShouldHandleErrorResponseFromSamlEngine() {
        throw new NotImplementedException("Test responsePostShouldHandleErrorResponseFromSamlEngine has not been implemented");
    }

    @Ignore
    @Test
    public void responsePostShouldHandleRequesterErrorResponse() {
        throw new NotImplementedException("Test responsePostShouldHandleRequesterErrorResponse has not been implemented");
    }

    @Ignore
    @Test
    public void responsePostShouldHandleFraudResponse() {
        throw new NotImplementedException("Test responsePostShouldHandleFraudResponse has not been implemented");
    }

    @Test
    public void responsePostShouldHandleAuthnFailedResponse() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");

        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-idp-authn-response"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{\"status\":\"AuthenticationFailed\"}"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", "some-session-id",
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", "some-session-id"),
            new AbstractMap.SimpleEntry<>("result", "OTHER"),
            new AbstractMap.SimpleEntry<>("isRegistration", true)
        );
    }

    @Test
    public void responsePostShouldHandleNoAuthnContextResponse() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");

        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-idp-authn-response"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{\"status\":\"NoAuthenticationContext\"}"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", "some-session-id",
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", "some-session-id"),
            new AbstractMap.SimpleEntry<>("result", "OTHER"),
            new AbstractMap.SimpleEntry<>("isRegistration", true)
        );
    }

    @Test
    public void responsePostShouldHandAuthnSuccessResponse() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");

        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        stubFor(
            get(urlEqualTo("/config/matching-services/https:%2F%2Fsome-matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"uri\": \"https://some-attribute-query-uri\"," +
                    "\"onboarding\": false" +
                    "}"
                ))
        );
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-idp-authn-response"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"status\":\"Success\"," +
                    "\"loaAchieved\":\"LEVEL_2\"," +
                    "\"encryptedMatchingDatasetAssertion\":\"some-mds-assertion\"," +
                    "\"authnStatementAssertion\":\"some-authn-statement-assertion\"," +
                    "\"persistentId\":\"some-persistent-id\"" +
                    "}"))
        );
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo("some-session-id"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", "some-session-id",
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", "some-session-id"),
            new AbstractMap.SimpleEntry<>("result", "SUCCESS"),
            new AbstractMap.SimpleEntry<>("isRegistration", true),
            new AbstractMap.SimpleEntry<>("loaAchieved", "LEVEL_2")
        );
    }

    @Ignore
    @Test
    public void responsePostShouldReturnBadRequestWhenIdpIsDifferentThanSelectedIdp() {
        throw new NotImplementedException("Test responsePostShouldReturnBadRequestWhenIdpIsDifferentThanSelectedIdp has not been implemented");
    }

    @Ignore
    @Test
    public void responsePostShouldReturnBadRequestWhenLevelOfAssuranceIsNotMet() {
        throw new NotImplementedException("Test responsePostShouldReturnBadRequestWhenLevelOfAssuranceIsNotMet has not been implemented");
    }

    @Ignore
    @Test
    public void responsePostShouldReturnForbiddenWhenIdpIsNotAvailable() {
        throw new NotImplementedException("Test responsePostShouldReturnForbiddenWhenIdpIsNotAvailable has not been implemented");
    }

    @Ignore
    @Test
    public void responsePostShouldReturnBadRequestSessionDoesNotExist() {
        throw new NotImplementedException("Test responsePostShouldReturnBadRequestSessionDoesNotExist has not been implemented");
    }
}
