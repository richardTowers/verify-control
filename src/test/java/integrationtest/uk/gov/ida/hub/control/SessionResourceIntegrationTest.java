package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.api.AuthnRequest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

    @Ignore
    @Test
    public void shouldReturnBadRequestWhenAssertionConsumerIndexIsInvalid() {
        throw new NotImplementedException("Test shouldReturnBadRequestWhenAssertionConsumerIndexIsInvalid has not been implemented");
    }

    @Ignore
    @Test
    public void shouldReturnInvalidSamlExceptionWhenSamlEngineThrowsInvalidSamlException() {
        throw new NotImplementedException("Test shouldReturnInvalidSamlExceptionWhenSamlEngineThrowsInvalidSamlException has not been implemented");
    }

    @Ignore
    @Test
    public void getSessionShouldFailWhenSessionDoesNotExist() {
        throw new NotImplementedException("Test getSessionShouldFailWhenSessionDoesNotExist has not been implemented");
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

    @Ignore
    @Test
    public void shouldUpdateSessionStateAndSendAnAttributeQueryRequestWhenASuccessResponseIsReceivedFromIdp() {
        throw new NotImplementedException("Test shouldUpdateSessionStateAndSendAnAttributeQueryRequestWhenASuccessResponseIsReceivedFromIdp has not been implemented");
    }

    @Ignore
    @Test
    public void selectIdpShouldReturnErrorWhenSessionHasTimedOut() {
        throw new NotImplementedException("Test selectIdpShouldReturnErrorWhenSessionHasTimedOut has not been implemented");
    }

    @Ignore
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
}