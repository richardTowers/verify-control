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

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class MatchingServiceResourcesIntegrationTest extends BaseVerifyControlIntegrationTest {

    @Ignore
    @Test
    public void shouldReturnOkWhenASuccessMatchingServiceResponseIsReceived() {
        throw new NotImplementedException("Test shouldReturnOkWhenASuccessMatchingServiceResponseIsReceived has not been implemented");
    }
    @Ignore
    @Test
    public void shouldReturnOkWhenAMatchingServiceFailureResponseIsReceived() {
        throw new NotImplementedException("Test shouldReturnOkWhenAMatchingServiceFailureResponseIsReceived has not been implemented");
    }
    @Ignore
    @Test
    public void responseFromMatchingService_shouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle1MatchRequest() {
        throw new NotImplementedException("Test responseFromMatchingService_shouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle1MatchRequest has not been implemented");
    }
    @Ignore
    @Test
    public void responseFromMatchingService_shouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle3MatchRequest() {
        throw new NotImplementedException("Test responseFromMatchingService_shouldThrowExceptionWhenInResponseToDoesNotMatchFromCycle3MatchRequest has not been implemented");
    }
    @Ignore
    @Test
    public void responseProcessingDetails_shouldReturnSuccessResponse_whenNoMatchWithC3Enabled_userAccountCreationAttributesAreFetched() {
        throw new NotImplementedException("Test responseProcessingDetails_shouldReturnSuccessResponse_whenNoMatchWithC3Enabled_userAccountCreationAttributesAreFetched has not been implemented");
    }
    @Ignore
    @Test
    public void responseProcessingDetails_shouldReturnSuccessResponse_whenNoMatchWithC3Disabled_userAccountCreationAttributesAreFetched() {
        throw new NotImplementedException("Test responseProcessingDetails_shouldReturnSuccessResponse_whenNoMatchWithC3Disabled_userAccountCreationAttributesAreFetched has not been implemented");
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
    public void isResponseFromHubReady_shouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle3() {
        throw new NotImplementedException("Test isResponseFromHubReady_shouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle3 has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReady_shouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle1() {
        throw new NotImplementedException("Test isResponseFromHubReady_shouldReturnFailedStatusWhenAProblemHasOccurredWhilstMatchingCycle1 has not been implemented");
    }
    @Ignore
    @Test
    public void getCycle3AttributeRequestData_shouldReturnExpectedAttributeData() {
        throw new NotImplementedException("Test getCycle3AttributeRequestData_shouldReturnExpectedAttributeData has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReady_shouldThrowExceptionWhenCycle3MatchingServiceWaitPeriodHasBeenExceeded() {
        throw new NotImplementedException("Test isResponseFromHubReady_shouldThrowExceptionWhenCycle3MatchingServiceWaitPeriodHasBeenExceeded has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReady_shouldThrowExceptionWhenMatchingServiceWaitPeriodHasBeenExceeded() {
        throw new NotImplementedException("Test isResponseFromHubReady_shouldThrowExceptionWhenMatchingServiceWaitPeriodHasBeenExceeded has not been implemented");
    }
    @Ignore
    @Test
    public void isResponseFromHubReady_shouldTellFrontendToShowErrorPageWhenMSRespondsButSamlEngineThrowsInvalidSamlError() {
        throw new NotImplementedException("Test isResponseFromHubReady_shouldTellFrontendToShowErrorPageWhenMSRespondsButSamlEngineThrowsInvalidSamlError has not been implemented");
    }
}
