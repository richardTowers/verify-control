package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
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
    @Test
    public void responsePostShouldHandleErrorResponseFromSamlEngine() {
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
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withStatus(400).withBody("{" +
                    "\"exceptionType\":\"INVALID_SAML\"," +
                    "\"clientMessage\":\"Some exception message\"" +
                    "}"))
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

        assertThat(response.getStatus()).isEqualTo(400);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("exceptionType", "INVALID_SAML")
        );
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.IdpSelected.NAME);
    }

    @Test
    public void responsePostShouldHandleRequesterErrorResponse() {
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
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"status\":\"RequesterError\"," +
                    "\"levelOfAssurance\":\"LEVEL_2\"" +
                    "}"))
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
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.AuthnFailed.NAME);
    }

    @Test
    public void responsePostShouldHandleFraudResponse() {
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
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"status\":\"RequesterError\"," +
                    "\"levelOfAssurance\":\"LEVEL_X\"," +
                    "\"fraudIndicator\":\"fraudIndicator\"" +
                    "}"))
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
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.FraudResponse.NAME);
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
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.AuthnFailed.NAME);
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
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.AuthnFailed.NAME);
    }

    @Test
    public void responsePostShouldHandAuthnSuccessResponse() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "selectedIdp", "https://some-idp-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");

        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/levels-of-assurance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"LEVEL_1\", \"LEVEL_2\"]"))
        );
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
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
                    "\"loaAchieved\":\"LEVEL_1\"," +
                    "\"issuer\":\"https://some-idp-entity-id\"," +
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
            new AbstractMap.SimpleEntry<>("loaAchieved", "LEVEL_1")
        );
    }

    @Test
    public void responsePostShouldReturnBadRequestWhenIdpIsDifferentThanSelectedIdp() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "selectedIdp", "https://some-idp-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");

        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/levels-of-assurance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"LEVEL_1\", \"LEVEL_2\"]"))
        );
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\",\"https://some-other-idp-entity-id\"]"))
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
                    "\"loaAchieved\":\"LEVEL_1\"," +
                    "\"issuer\":\"https://some-other-idp-entity-id\"," +
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

        assertThat(response.getStatus()).isEqualTo(400);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("exceptionType", "STATE_PROCESSING_VALIDATION")
        );
    }

    @Test
    public void responsePostShouldReturnBadRequestWhenLevelOfAssuranceIsNotMet() {
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
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/levels-of-assurance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"LEVEL_2\"]"))
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
                    "\"loaAchieved\":\"LEVEL_1\"," +
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

        assertThat(response.getStatus()).isEqualTo(400);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("exceptionType", "STATE_PROCESSING_VALIDATION")
        );
    }

    @Test
    public void responsePostShouldReturnForbiddenWhenIdpIsNotAvailable() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "selectedIdp", "https://some-idp-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");

        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
        );
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/levels-of-assurance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"LEVEL_1\", \"LEVEL_2\"]"))
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
                    "\"loaAchieved\":\"LEVEL_1\"," +
                    "\"issuer\":\"https://some-other-idp-entity-id\"," +
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

        assertThat(response.getStatus()).isEqualTo(403);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("exceptionType", "IDP_DISABLED")
        );
    }

    @Test
    public void responsePostShouldReturnBadRequestSessionDoesNotExist() {
        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", "some-session-id",
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(400);
    }
}
