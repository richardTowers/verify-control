package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import java.util.AbstractMap;
import java.util.Map;

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

    @Test
    public void shouldUpdateSessionStateToCancelledCycle3InputStateWhenInputToCycle3IsCancelled() {
        redisClient.set("state:some-session-id", VerifySessionState.AwaitingCycle3Data.NAME);
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");

        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo("some-session-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        var response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/cycle-3-attribute/cancel", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(null)
            .invoke();

        assertThat(response.getStatus()).isEqualTo(204);
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.Cycle3Cancelled.NAME);
    }

    @Test
    public void shouldGetCycle3AttributeRequestDataFromConfiguration() {
        redisClient.set("state:some-session-id", VerifySessionState.AwaitingCycle3Data.NAME);
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");

        configureFor(configPort());
        stubFor(
            get(urlPathEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-process"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{\"attributeName\":\"some-attribute-name\"}"))
        );
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo("some-session-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        var response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/cycle-3-attribute", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .buildGet()
            .invoke();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.AwaitingCycle3Data.NAME);
        var responseBody = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("attributeName", "some-attribute-name"),
            new AbstractMap.SimpleEntry<>("requestIssuerId", "https://some-service-entity-id")
        );
    }
}
