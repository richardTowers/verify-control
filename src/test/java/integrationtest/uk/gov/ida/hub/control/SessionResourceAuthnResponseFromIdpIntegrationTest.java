package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class SessionResourceAuthnResponseFromIdpIntegrationTest extends BaseVerifyControlIntegrationTest {

    private String sessionId;

    @Before
    public void setUp() {
        sessionId = UUID.randomUUID().toString();
    }

    @Test
    public void responsePostShouldHandleErrorResponseFromSamlEngine() {
        createSession();

        configureConfigStub("[]", "[]");
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
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(400);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("exceptionType", "INVALID_SAML")
        );
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.IdpSelected.NAME);
    }

    @Test
    public void responsePostShouldHandleRequesterErrorResponse() {
        createSession();

        configureConfigStub("[]", "[]");
        configureSamlEngineStub("RequesterError", "LEVEL_2", null, null);

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", sessionId),
            new AbstractMap.SimpleEntry<>("result", "OTHER"),
            new AbstractMap.SimpleEntry<>("isRegistration", true)
        );
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.AuthnFailed.NAME);
    }

    @Test
    public void responsePostShouldHandleFraudResponse() {
        createSession();

        configureConfigStub("[]", "[]");
        configureSamlEngineStub("RequesterError", "LEVEL_X", "fraudIndicator", null);

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", sessionId),
            new AbstractMap.SimpleEntry<>("result", "OTHER"),
            new AbstractMap.SimpleEntry<>("isRegistration", true)
        );
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.FraudResponse.NAME);
    }

    @Test
    public void responsePostShouldHandleAuthnFailedResponse() {
        createSession();

        configureConfigStub("[]", "[]");
        configureSamlEngineStub("AuthenticationFailed", null, null, null);

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", sessionId),
            new AbstractMap.SimpleEntry<>("result", "OTHER"),
            new AbstractMap.SimpleEntry<>("isRegistration", true)
        );
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.AuthnFailed.NAME);
    }

    @Test
    public void responsePostShouldHandleNoAuthnContextResponse() {
        createSession();

        configureConfigStub("[]", "[]");
        configureSamlEngineStub("NoAuthenticationContext", null, null, null);

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", sessionId),
            new AbstractMap.SimpleEntry<>("result", "OTHER"),
            new AbstractMap.SimpleEntry<>("isRegistration", true)
        );
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.AuthnFailed.NAME);
    }

    @Test
    public void responsePostShouldHandAuthnSuccessResponse() {
        createSession();

        configureConfigStub("[\"LEVEL_1\", \"LEVEL_2\"]", "[\"https://some-idp-entity-id\"]");
        configureSamlEngineStub("Success", "LEVEL_1", null, null);
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo(sessionId))
            .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {{ }});
        assertThat(responseBody).containsExactly(
            new AbstractMap.SimpleEntry<>("sessionId", sessionId),
            new AbstractMap.SimpleEntry<>("result", "SUCCESS"),
            new AbstractMap.SimpleEntry<>("isRegistration", true),
            new AbstractMap.SimpleEntry<>("loaAchieved", "LEVEL_1")
        );
    }

    @Test
    public void responsePostShouldReturnBadRequestWhenIdpIsDifferentThanSelectedIdp() {
        createSession();

        configureConfigStub("[\"LEVEL_1\", \"LEVEL_2\"]", "[\"https://some-idp-entity-id\",\"https://some-other-idp-entity-id\"]");
        configureSamlEngineStub("Success", "LEVEL_1", null, "https://some-other-idp-entity-id");
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo(sessionId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
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
        createSession();

        configureConfigStub("[\"LEVEL_2\"]", "[]");
        configureSamlEngineStub("Success", "LEVEL_1", null, null);
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo(sessionId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
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
        createSession();

        configureConfigStub("[\"LEVEL_1\", \"LEVEL_2\"]", "[\"https://some-idp-entity-id\"]");
        configureSamlEngineStub("Success", "LEVEL_1", null, "https://some-other-idp-entity-id");

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
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
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(400);
    }

    private void createSession() {
        redisClient.set("state:" + sessionId, VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:" + sessionId, "issuer", "https://some-service-entity-id");
        redisClient.hset("session:" + sessionId, "selectedIdp", "https://some-idp-entity-id");
        redisClient.hset("session:" + sessionId, "requestId", "some-request-id");
        redisClient.hset("session:" + sessionId, "isRegistration", Boolean.toString(true));
    }

    private void configureConfigStub(String levelsOfAssurance, String enabledIdps) {
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/levels-of-assurance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(levelsOfAssurance))
        );
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(enabledIdps))
        );
        stubFor(
            get(urlEqualTo("/config/matching-services/https:%2F%2Fsome-matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"uri\": \"https://some-attribute-query-uri\"," +
                    "\"onboarding\": false" +
                    "}"
                ))
        );
    }

    private void configureSamlEngineStub(String status, String levelOfAssurance, String fraudIndicator, String idpEntityId) {
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-idp-authn-response"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"status\":\"" + status + "\"," +
                    "\"issuer\":\"" + (idpEntityId == null ? "https://some-idp-entity-id" : idpEntityId) + "\"," +
                    "\"encryptedMatchingDatasetAssertion\":\"some-mds-assertion\"," +
                    "\"authnStatementAssertion\":\"some-authn-statement-assertion\"," +
                    "\"persistentId\":\"some-persistent-id\"" +
                    (levelOfAssurance == null ? "" : ",\"loaAchieved\":\"" + levelOfAssurance + "\"") +
                    (fraudIndicator == null ? "" : ",\"fraudIndicator\": \"" + fraudIndicator + "\"") +
                    "}"))
        );
    }
}
