package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.junit.Test;
import uk.gov.ida.hub.control.api.AuthnRequest;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionResourceIntegrationTest extends BaseVerifyControlIntegrationTest {
    @Test
    public void shouldCreateSession() {
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
        assertThat(session.get("issuer")).isEqualTo("TODO: issuer");
        assertThat(session.get("requestId")).isEqualTo("TODO: requestId");
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

    private Response createASession(AuthnRequest authnRequest) {
        var url = String.format("http://localhost:%s/policy/session", verifyControl.getLocalPort());
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(authnRequest, MediaType.APPLICATION_JSON_TYPE));
    }
}
