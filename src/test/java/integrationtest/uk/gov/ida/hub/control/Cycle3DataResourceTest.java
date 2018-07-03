package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.client.Entity;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class Cycle3DataResourceTest extends BaseVerifyControlIntegrationTest {
    @Test
    public void should_ReturnSuccessWhenDataSubmitted() {
        redisClient.set("state:some-session-id", VerifySessionState.AwaitingCycle3Data.NAME);
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");

        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo("some-session-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        var response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/cycle-3-attribute/submit", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(Entity.entity(mapOf(
                "cycle3Input", "some-cycle-3-input-value",
                "principalIpAddress", "some-ip-address"
            ), APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.Cycle3MatchRequestSent.NAME);
    }

    @Ignore
    @Test
    public void shouldUpdateSessionStateToCancelledCycle3InputStateWhenInputToCycle3IsCancelled() {
        throw new NotImplementedException("Test shouldUpdateSessionStateToCancelledCycle3InputStateWhenInputToCycle3IsCancelled has not been implemented");
    }

    @Ignore
    @Test
    public void shouldGetCycle3AttributeRequestDataFromConfiguration() {
        throw new NotImplementedException("Test shouldGetCycle3AttributeRequestDataFromConfiguration has not been implemented");
    }
}
