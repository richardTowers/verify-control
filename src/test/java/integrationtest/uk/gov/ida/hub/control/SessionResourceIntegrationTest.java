package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.api.AuthnRequest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class SessionResourceIntegrationTest extends BaseVerifyControlIntegrationTest {
    @Test
    public void shouldCreateSession() {
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-rp-authn-request")).willReturn(
                aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                    .withBody("{\"issuer\":\"some-issuer\",\"requestId\":\"some-request-id\"}")
            )
        );

        var response = createASession(
            new AuthnRequest(
                "some-saml-request",
                "some-relay-state",
                "some-ip-address"
            )
        );
        var sessionId = response.readEntity(String.class);
        assertThat(response.getStatus()).isEqualTo(201);

        var session = redisClient.hgetall("session:" + sessionId);
        assertThat(session.get("start")).isNotBlank();
        assertThat(session.get("issuer")).isEqualTo("some-issuer");
        assertThat(session.get("requestId")).isEqualTo("some-request-id");
        assertThat(session.get("relayState")).isEqualTo("some-relay-state");
        assertThat(session.get("ipAddress")).isEqualTo("some-ip-address");
    }

    @Test
    public void shouldValidateMissingCreateSessionParameters() {
        var response = createASession(new AuthnRequest(null, null, null));
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void shouldValidateMissingCreateSessionBody() {
        var response = createASession(null);
        assertThat(response.getStatus()).isEqualTo(422);
    }

    @Test
    public void shouldReturnBadRequestWhenAssertionConsumerIndexIsInvalid() {
        // Deliberately not implementing lookup of assertionConsumerService URLs in config.
        // This feature doesn't seem to be valuable enough to make up for the extra complexity.
        assertThat("The assertionConsumerService lookup feature").isNotEqualTo("Something we should implement.");
    }

    @Ignore
    @Test
    public void shouldReturnInvalidSamlExceptionWhenSamlEngineThrowsInvalidSamlException() {
        throw new NotImplementedException("Test shouldReturnInvalidSamlExceptionWhenSamlEngineThrowsInvalidSamlException has not been implemented");
    }

    @Test
    public void getSessionShouldFailWhenSessionDoesNotExist() {
        var response = getSession("some-non-existent-session-id");

        assertThat(response.getStatus()).isEqualTo(400);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>(){});
        assertThat(responseBody.get("errorId")).isNotNull().isNotEqualTo("");
        assertThat(responseBody.get("exceptionType")).isEqualTo("SESSION_NOT_FOUND");
        assertThat(responseBody.get("clientMessage")).isEqualTo("");
        assertThat(responseBody.get("audited")).isEqualTo(false);
    }

    @Test
    public void shouldReturnOkWhenGeneratingIdpAuthnRequestFromHubIsSuccessfulOnSignIn() {
        redisClient.hset("session:some-session-id", "isRegistration", "false");
        redisClient.hset("session:some-session-id", "selectedIdp", "https://some-idp-entity-id");

        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/generate-idp-authn-request"))
                .withRequestBody(matchingJsonPath("idpEntityId", equalTo("https://some-idp-entity-id")))
                .withRequestBody(matchingJsonPath("levelsOfAssurance[0]", equalTo("LEVEL_1")))
                .willReturn(
                aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                    .withBody("{\"samlRequest\":\"some-saml-request\",\"ssoUri\":\"https://some-sso-uri\"}")
            )
        );
        var response = generateIdpAuthnRequest("some-session-id");
        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertThat(responseBody.get("samlRequest")).isEqualTo("some-saml-request");
        assertThat(responseBody.get("postEndpoint")).isEqualTo("https://some-sso-uri");
        assertThat(responseBody.get("registering")).isEqualTo(false);
    }

    @Test
    public void shouldReturnOkWhenGeneratingIdpAuthnRequestFromHubIsSuccessfulOnRegistration() {
        redisClient.hset("session:some-session-id", "isRegistration", "true");
        redisClient.hset("session:some-session-id", "selectedIdp", "https://some-idp-entity-id");

        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/generate-idp-authn-request"))
                .withRequestBody(matchingJsonPath("idpEntityId", equalTo("https://some-idp-entity-id")))
                .withRequestBody(matchingJsonPath("levelsOfAssurance[0]", equalTo("LEVEL_1")))
                .willReturn(
                  aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                    .withBody("{\"samlRequest\":\"some-saml-request\",\"ssoUri\":\"https://some-sso-uri\"}")
                )
        );
        var response = generateIdpAuthnRequest("some-session-id");
        assertThat(response.getStatus()).isEqualTo(200);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertThat(responseBody.get("samlRequest")).isEqualTo("some-saml-request");
        assertThat(responseBody.get("postEndpoint")).isEqualTo("https://some-sso-uri");
        assertThat(responseBody.get("registering")).isEqualTo(true);
    }

    @Test
    public void shouldReturnNotFoundWhenSessionDoesNotExistInPolicy() {
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/generate-idp-authn-request"))
                .withRequestBody(matchingJsonPath("idpEntityId", equalTo("https://some-idp-entity-id")))
                .withRequestBody(matchingJsonPath("levelsOfAssurance[0]", equalTo("LEVEL_1")))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                        .withBody("{\"samlRequest\":\"some-saml-request\",\"ssoUri\":\"https://some-sso-uri\"}")
                )
        );
        var response = generateIdpAuthnRequest("some-session-id");
        assertThat(response.getStatus()).isEqualTo(400);
        var responseBody = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertThat(responseBody.get("exceptionType")).isEqualTo("SESSION_NOT_FOUND");
    }

    @Ignore
    @Test
    public void shouldGetRpResponseGivenASessionExistsInPolicy() {
        throw new NotImplementedException("Test shouldGetRpResponseGivenASessionExistsInPolicy has not been implemented");
    }

    private Response createASession(AuthnRequest authnRequest) {
        var url = String.format("http://localhost:%s/policy/session", verifyControl.getLocalPort());
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(authnRequest, MediaType.APPLICATION_JSON_TYPE));
    }

    private Response getSession(String sessionId) {
        var url = String.format("http://localhost:%d/policy/session/%s", verifyControl.getLocalPort(), sessionId);
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
    }

    private Response generateIdpAuthnRequest(String sessionId) {
        var url = String.format("http://localhost:%d/policy/session/%s/idp-authn-request-from-hub", verifyControl.getLocalPort(), sessionId);
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get();
    }

}
