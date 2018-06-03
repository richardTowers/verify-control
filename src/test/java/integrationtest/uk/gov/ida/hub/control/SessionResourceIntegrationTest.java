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
            post(urlEqualTo("/translate-rp-authn-request")).willReturn(
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

    @Ignore
    @Test
    public void shouldReturnOkWhenGeneratingIdpAuthnRequestFromHubIsSuccessfulOnSignIn() {
        throw new NotImplementedException("Test shouldReturnOkWhenGeneratingIdpAuthnRequestFromHubIsSuccessfulOnSignIn has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnOkWhenGeneratingIdpAuthnRequestFromHubIsSuccessfulOnRegistration() {
        throw new NotImplementedException("Test shouldReturnOkWhenGeneratingIdpAuthnRequestFromHubIsSuccessfulOnRegistration has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnNotFoundWhenSessionDoesNotExistInPolicy() {
        throw new NotImplementedException("Test shouldReturnNotFoundWhenSessionDoesNotExistInPolicy has not been implemented");
    }

    @Ignore
    @Test
    public void shouldGetRpResponseGivenASessionExistsInPolicy() {
        throw new NotImplementedException("Test shouldGetRpResponseGivenASessionExistsInPolicy has not been implemented");
    }

    @Ignore
    @Test
    public void shouldGetRpResponseGivenASessionInEidasSuccessfulMatchStateExists() {
        throw new NotImplementedException("Test shouldGetRpResponseGivenASessionInEidasSuccessfulMatchStateExists has not been implemented");
    }

    @Test
    public void selectIdpShouldReturnErrorWhenSessionDoesNotExistInPolicy() {
        throw new NotImplementedException("Test selectIdpShouldReturnErrorWhenSessionDoesNotExistInPolicy has not been implemented");
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
}
