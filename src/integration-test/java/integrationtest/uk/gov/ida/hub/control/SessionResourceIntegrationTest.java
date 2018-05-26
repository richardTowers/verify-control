package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionResourceIntegrationTest extends BaseVerifyControlIntegrationTest {
    @Test
    public void shouldCreateSession() {
        var response = createASession();
        var sessionId = response.readEntity(String.class);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(redisClient.get("session:" + sessionId)).isEqualTo("banana");
    }

    private Response createASession() {
        var url = String.format("http://localhost:%s/policy/session", verifyControl.getLocalPort());
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(null);
    }
}
