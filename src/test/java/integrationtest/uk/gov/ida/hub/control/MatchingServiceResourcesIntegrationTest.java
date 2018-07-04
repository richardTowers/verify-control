package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.AbstractMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class MatchingServiceResourcesIntegrationTest extends BaseVerifyControlIntegrationTest {

    @Test
    public void shouldReturnOkWhenASuccessMatchingServiceResponseIsReceived() {
        redisClient.set("state:some-session-id", VerifySessionState.Cycle0And1MatchRequestSent.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");

        configureFor(samlEnginePort());
        stubFor(
            post(
                urlPathEqualTo("/saml-engine/translate-attribute-query")).willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBody("{\"status\":\"MatchingServiceMatch\"}")
                )
        );

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/attribute-query-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlResponse", "some-saml-response"), APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.Match.NAME);
    }

    @Ignore
    @Test
    public void shouldReturnOkWhenAMatchingServiceFailureResponseIsReceived() {
        throw new NotImplementedException("Test shouldReturnOkWhenAMatchingServiceFailureResponseIsReceived has not been implemented");
    }
    @Ignore
    @Test
    public void responseFromMatchingServiceShouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle1MatchRequest() {
        throw new NotImplementedException("Test responseFromMatchingServiceShouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle1MatchRequest has not been implemented");
    }
    @Ignore
    @Test
    public void responseFromMatchingServiceShouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle3MatchRequest() {
        throw new NotImplementedException("Test responseFromMatchingServiceShouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle3MatchRequest has not been implemented");
    }
    @Ignore
    @Test
    public void responseProcessingDetailsShouldReturnSuccessResponseWhenNoMatchWithC3EnabledUserAccountCreationAttributesAreFetched() {
        throw new NotImplementedException("Test responseProcessingDetailsShouldReturnSuccessResponseWhenNoMatchWithC3EnabledUserAccountCreationAttributesAreFetched has not been implemented");
    }
    @Ignore
    @Test
    public void responseProcessingDetailsShouldReturnSuccessResponseWhenNoMatchWithC3DisabledUserAccountCreationAttributesAreFetched() {
        throw new NotImplementedException("Test responseProcessingDetailsShouldReturnSuccessResponseWhenNoMatchWithC3DisabledUserAccountCreationAttributesAreFetched has not been implemented");
    }
    @Ignore
    @Test
    public void fullSuccessfulJourneyThroughAllStates() {
        throw new NotImplementedException("Test fullSuccessfulJourneyThroughAllStates has not been implemented");
    }
    @Ignore
    @Test
    public void journeyWithFailedAccountCreation() {
        throw new NotImplementedException("Test journeyWithFailedAccountCreation has not been implemented");
    }

    @Test
    public void responseProcessingDetailsShouldReturnWaitingForC3StatusWhenNoMatchResponseSentFromMatchingServiceAndC3Required() {
        redisClient.set("state:some-session-id", VerifySessionState.Cycle0And1MatchRequestSent.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");

        configureFor(samlEnginePort());
        stubFor(
            post(
                urlPathEqualTo("/saml-engine/translate-attribute-query")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody("{\"status\":\"NoMatchingServiceMatchFromMatchingService\"}")
            )
        );

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/attribute-query-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlResponse", "some-saml-response"), APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.AwaitingCycle3Data.NAME);
        var responseBody = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(responseBody).contains(
            new AbstractMap.SimpleEntry<>("responseProcessingStatus", "GET_C3_DATA"),
            new AbstractMap.SimpleEntry<>("sessionId", "some-session-id")
        );
    }

    @Ignore
    @Test
    public void isResponseFromHubReadyShouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle3() {
        throw new NotImplementedException("Test isResponseFromHubReadyShouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle3 has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReadyShouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle1() {
        throw new NotImplementedException("Test isResponseFromHubReadyShouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle1 has not been implemented");
    }
    @Ignore
    @Test
    public void getCycle3AttributeRequestDataShouldReturnExpectedAttributeData() {
        throw new NotImplementedException("Test getCycle3AttributeRequestDataShouldReturnExpectedAttributeData has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReadyShouldThrowExceptionWhenCycle3MatchingServiceWaitPeriodHasBeenExceeded() {
        throw new NotImplementedException("Test isResponseFromHubReadyShouldThrowExceptionWhenCycle3MatchingServiceWaitPeriodHasBeenExceeded has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReadyShouldThrowExceptionWhenMatchingServiceWaitPeriodHasBeenExceeded() {
        throw new NotImplementedException("Test isResponseFromHubReadyShouldThrowExceptionWhenMatchingServiceWaitPeriodHasBeenExceeded has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReadyShouldTellFrontendToShowErrorPageWhenMSRespondsButSamlEngineThrowsInvalidSamlError() {
        throw new NotImplementedException("Test isResponseFromHubReadyShouldTellFrontendToShowErrorPageWhenMSRespondsButSamlEngineThrowsInvalidSamlError has not been implemented");
    }
}
