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
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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

    @Test
    public void fullSuccessfulJourneyThroughAllStates() {
        // TODO: get rid of these and initialise the session by calling the API
        redisClient.set("state:some-session-id", VerifySessionState.Started.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        redisClient.hset("session:some-session-id", "isRegistration", "true");
        redisClient.hset("session:some-session-id", "requestId", "some-request-id");


        anIdpIsSelectedForRegistration();
        anIdpAuthnRequestWasGenerated();
        anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent();
        aNoMatchResponseWasReceivedFromTheMSA();
        aCycle3AttributeHasBeenSentToPolicyFromTheUser();
        aNoMatchResponseWasReceivedFromTheMSA();
        // aUserAccountCreationResponseIsReceived();

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

        var response = aNoMatchResponseWasReceivedFromTheMSA();

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

    private void anIdpIsSelectedForRegistration() {
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
        );
        var url = String.format("http://localhost:%d/policy/received-authn-request/%s/select-identity-provider", verifyControl.getLocalPort(), "some-session-id");
        var response = httpClient
            .target(url)
            .request(APPLICATION_JSON_TYPE)
            .post(entity(
                mapOf(
                    "selectedIdpEntityId", "https://some-idp-entity-id",
                    "principalIpAddress", "8.8.8.8",
                    "registration", true,
                    "requestedLoa", "LEVEL_1"
                ),
                APPLICATION_JSON_TYPE)
            );
        assertThat(response.getStatus()).isEqualTo(201);
    }

    private void anIdpAuthnRequestWasGenerated() {
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/generate-idp-authn-request"))
                .withRequestBody(matchingJsonPath("idpEntityId", equalTo("https://some-idp-entity-id")))
                .withRequestBody(matchingJsonPath("levelsOfAssurance[0]", equalTo("LEVEL_1")))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody("{\"samlRequest\":\"some-saml-request\",\"ssoUri\":\"https://some-sso-uri\"}")
                )
        );
        var url = String.format("http://localhost:%d/policy/session/%s/idp-authn-request-from-hub", verifyControl.getLocalPort(), "some-session-id");
        var response = httpClient
            .target(url)
            .request(APPLICATION_JSON_TYPE)
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent() {
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("https://some-matching-service-entity-id"))
        );
        stubFor(
            get(urlEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/levels-of-assurance"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"LEVEL_1\", \"LEVEL_2\"]"))
        );
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
        );
        stubFor(
            get(urlEqualTo("/config/matching-services/https:%2F%2Fsome-matching-service-entity-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"uri\": \"https://some-attribute-query-uri\"," +
                    "\"onboarding\": false" +
                    "}"
                ))
        );
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-idp-authn-response"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"status\":\"Success\"," +
                    "\"issuer\":\"https://some-idp-entity-id\"," +
                    "\"encryptedMatchingDatasetAssertion\":\"some-mds-assertion\"," +
                    "\"authnStatementAssertion\":\"some-authn-statement-assertion\"," +
                    "\"persistentId\":\"some-persistent-id\"," +
                    "\"loaAchieved\":\"LEVEL_1\"" +
                    "}"))
        );
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo("some-session-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", "some-session-id",
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .post(entity(request, APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private Response aNoMatchResponseWasReceivedFromTheMSA() {
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
        return response;
    }

    private void aCycle3AttributeHasBeenSentToPolicyFromTheUser() {
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo("some-session-id"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        var response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/cycle-3-attribute/submit", verifyControl.getLocalPort(), "some-session-id"))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf(
                "cycle3Input", "some-cycle-3-input-value",
                "principalIpAddress", "some-ip-address"
            ), APPLICATION_JSON_TYPE))
            .invoke();
        assertThat(response.getStatus()).isEqualTo(204);
    }

    private void aUserAccountCreationResponseIsReceived() {
        throw new NotImplementedException("TODO");
    }

}
