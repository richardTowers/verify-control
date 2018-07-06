package integrationtest.uk.gov.ida.hub.control;

import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.api.AuthnRequest;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
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
        var sessionId = "some-session-id";
        redisClient.set("state:" + sessionId, VerifySessionState.Cycle0And1MatchRequestSent.class.getSimpleName());
        redisClient.hset("session:" + sessionId, "issuer", "https://some-service-entity-id");

        configureFor(samlEnginePort());
        stubFor(
            post(
                urlPathEqualTo("/saml-engine/translate-attribute-query")).willReturn(
                    aResponse().withHeader("Content-Type", "application/json").withBody("{\"status\":\"MatchingServiceMatch\"}")
                )
        );

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/attribute-query-response", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlResponse", "some-saml-response"), APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.Match.class.getSimpleName());
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

    @Test
    public void responseProcessingDetailsShouldReturnSuccessResponseWhenNoMatchWithC3EnabledUserAccountCreationAttributesAreFetched() {
        var sessionId = aSessionIsCreated();
        anIdpIsSelectedForRegistration(sessionId);
        anIdpAuthnRequestWasGenerated(sessionId);
        anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, "some-cycle-3-attribute");
        aCycle3AttributeHasBeenSentToPolicyFromTheUser(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, "some-cycle-3-attribute");

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/response-from-idp/response-processing-details", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.UserAccountCreationRequestSent.class.getSimpleName());
        var responseBody = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(responseBody.get("responseProcessingStatus")).isEqualTo("WAIT");
        assertThat(responseBody.get("sessionId")).isEqualTo(sessionId);
    }

    @Test
    public void responseProcessingDetailsShouldReturnSuccessResponseWhenNoMatchWithC3DisabledUserAccountCreationAttributesAreFetched() {
        var sessionId = aSessionIsCreated();
        anIdpIsSelectedForRegistration(sessionId);
        anIdpAuthnRequestWasGenerated(sessionId);
        anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, null);

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/response-from-idp/response-processing-details", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.UserAccountCreationRequestSent.class.getSimpleName());
        var responseBody = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(responseBody.get("responseProcessingStatus")).isEqualTo("WAIT");
        assertThat(responseBody.get("sessionId")).isEqualTo(sessionId);
    }

    @Test
    public void fullSuccessfulJourneyThroughAllStates() {
        var sessionId = aSessionIsCreated();
        anIdpIsSelectedForRegistration(sessionId);
        anIdpAuthnRequestWasGenerated(sessionId);
        anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, "some-cycle-3-attribute");
        aCycle3AttributeHasBeenSentToPolicyFromTheUser(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, "some-cycle-3-attribute");
        aUserAccountCreationResponseIsReceived(sessionId, null);

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/response-from-idp/response-processing-details", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.UserAccountCreated.class.getSimpleName());
    }

    @Test
    public void journeyWithFailedAccountCreation() {
        var sessionId = aSessionIsCreated();
        anIdpIsSelectedForRegistration(sessionId);
        anIdpAuthnRequestWasGenerated(sessionId);
        anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, "some-session-id");
        aCycle3AttributeHasBeenSentToPolicyFromTheUser(sessionId);
        aNoMatchResponseWasReceivedFromTheMSA(sessionId, "some-session-id");
        aUserAccountCreationResponseIsReceived(sessionId, "UserAccountCreationFailed");

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/response-from-idp/response-processing-details", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        var responseProcessingDetails = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(responseProcessingDetails.get("responseProcessingStatus")).isEqualTo("USER_ACCOUNT_CREATION_FAILED");
        assertThat(responseProcessingDetails.get("sessionId")).isEqualTo(sessionId);
        assertThat(redisClient.get("state:" + sessionId)).isEqualTo(VerifySessionState.MatchingFailed.class.getSimpleName());
    }

    @Test
    public void responseProcessingDetailsShouldReturnWaitingForC3StatusWhenNoMatchResponseSentFromMatchingServiceAndC3Required() {
        redisClient.set("state:some-session-id", VerifySessionState.Cycle0And1MatchRequestSent.class.getSimpleName());
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");

        var response = aNoMatchResponseWasReceivedFromTheMSA("some-session-id", "some-cycle-3-attribute");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(redisClient.get("state:some-session-id")).isEqualTo(VerifySessionState.AwaitingCycle3Data.class.getSimpleName());
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

    private String aSessionIsCreated() {
        configureFor(samlEnginePort());
        stubFor(
            post(urlEqualTo("/saml-engine/translate-rp-authn-request")).willReturn(
                aResponse()
                    .withHeader("Content-Type", MediaType.APPLICATION_JSON)
                    .withBody("{\"issuer\":\"https://some-service-entity-id\",\"requestId\":\"some-request-id\"}")
            )
        );
        return httpClient
            .target(String.format("http://localhost:%s/policy/session", verifyControl.getLocalPort()))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(new AuthnRequest("some-saml-request", "some-relay-state", "some-principal-ip-address"), MediaType.APPLICATION_JSON_TYPE))
            .readEntity(String.class);
    }

    private void anIdpIsSelectedForRegistration(String sessionId) {
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/LEVEL_1/enabled"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
        );
        var url = String.format("http://localhost:%d/policy/received-authn-request/%s/select-identity-provider", verifyControl.getLocalPort(), sessionId);
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

    private void anIdpAuthnRequestWasGenerated(String sessionId) {
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
        var url = String.format("http://localhost:%d/policy/session/%s/idp-authn-request-from-hub", verifyControl.getLocalPort(), sessionId);
        var response = httpClient
            .target(url)
            .request(APPLICATION_JSON_TYPE)
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void anAuthnResponseFromIdpWasReceivedAndMatchingRequestSent(String sessionId) {
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
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo(sessionId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        Map<String, String> request = mapOf(
            "samlResponse", "some-saml-response",
            "sessionId", sessionId,
            "principalIPAddressAsSeenByHub", "some-ip-address"
        );
        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/idp-authn-response", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .post(entity(request, APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private Response aNoMatchResponseWasReceivedFromTheMSA(String sessionId, String cycle3AttributeName) {
        configureFor(samlEnginePort());
        stubFor(
            post(
                urlPathEqualTo("/saml-engine/translate-attribute-query")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody("{\"status\":\"NoMatchingServiceMatchFromMatchingService\"}")
            )
        );
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/matching-services/https:%2F%2Fsome-matching-service-entity-id/user-account-creation-attributes"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[{}]"))
        );
        stubFor(
            get(urlPathEqualTo("/config/transactions/https:%2F%2Fsome-service-entity-id/matching-process"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"attributeName\":" + (cycle3AttributeName == null ? "null" : "\"" + cycle3AttributeName + "\"") +
                    "}"))
        );

        Response response = httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/attribute-query-response", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlResponse", "some-saml-response"), APPLICATION_JSON_TYPE))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(200);
        return response;
    }

    private void aCycle3AttributeHasBeenSentToPolicyFromTheUser(String sessionId) {
        configureFor(samlSoapProxyPort());
        stubFor(
            post(urlPathEqualTo("/matching-service-request-sender")).withQueryParam("sessionId", equalTo(sessionId))
                .willReturn(aResponse().withHeader("Content-Type", "application/json"))
        );

        var response = httpClient
            .target(String.format("http://localhost:%d/policy/received-authn-request/%s/cycle-3-attribute/submit", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf(
                "cycle3Input", "some-cycle-3-input-value",
                "principalIpAddress", "some-ip-address"
            ), APPLICATION_JSON_TYPE))
            .invoke();
        assertThat(response.getStatus()).isEqualTo(204);
    }

    private void aUserAccountCreationResponseIsReceived(String sessionId, String status) {
        configureFor(samlEnginePort());
        stubFor(
            post(
                urlPathEqualTo("/saml-engine/translate-attribute-query")).willReturn(
                aResponse().withHeader("Content-Type", "application/json").withBody("{" +
                    "\"status\":\"" + (status == null ? "UserAccountCreated" : status) + "\"" +
                    "}")
            )
        );
        httpClient
            .target(String.format("http://localhost:%d/policy/session/%s/attribute-query-response", verifyControl.getLocalPort(), sessionId))
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf("samlResponse", "some-saml-response"), APPLICATION_JSON_TYPE))
            .invoke();
    }

}
