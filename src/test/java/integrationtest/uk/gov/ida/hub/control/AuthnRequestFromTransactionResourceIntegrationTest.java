package integrationtest.uk.gov.ida.hub.control;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import integrationtest.uk.gov.ida.hub.control.helpers.BaseVerifyControlIntegrationTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class AuthnRequestFromTransactionResourceIntegrationTest extends BaseVerifyControlIntegrationTest {
    @Test
    public void badEntityResponseThrownWhenMandatoryFieldsAreMissing() throws IOException {
        var url = String.format("http://localhost:%d/policy/received-authn-request/%s/select-identity-provider", verifyControl.getLocalPort(), "some-session-id");
        var response = httpClient.target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(ImmutableMap.of(), MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(422);

        var msg = verifyControl.getObjectMapper().readTree(response.readEntity(String.class));
        var errors = Streams.stream(msg.get("errors").elements()).map(JsonNode::textValue).collect(Collectors.toList());
        assertThat(errors).contains("selectedIdpEntityId may not be empty");
        assertThat(errors).contains("principalIpAddress may not be empty");
        assertThat(errors).contains("registration may not be null");
        assertThat(errors).contains("requestedLoa may not be null");
    }

    @Test
    public void selectIdpShouldReturnSuccessResponseAndAudit() {
        redisClient.set("state:some-session-id", VerifySessionState.Started.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/enabled-for-signin"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
        );
        var response = selectIdp("https://some-idp-entity-id", "LEVEL_1", false, "some-session-id");
        assertThat(response.getStatus()).isEqualTo(201);
        // TODO: (event sink) This test should check that event sink was called.
    }

    @Test
    public void idpSelectedShouldThrowIfIdpIsNotAvailable() {
        redisClient.set("state:some-session-id", VerifySessionState.Started.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/enabled-for-signin"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[]"))
        );
        var response = selectIdp("https://some-none-existent-idp-entity-id", "LEVEL_1", false, "some-session-id");
        assertThat(response.getStatus()).isEqualTo(400);
        var error = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(error.get("exceptionType")).isEqualTo("STATE_PROCESSING_VALIDATION");
    }

    @Test
    public void idpSelectedShouldThrowIfSessionInWrongState() {
        redisClient.set("state:some-session-id", VerifySessionState.Match.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        configureFor(configPort());
        stubFor(
            get(urlEqualTo("/config/idps/https:%2F%2Fsome-service-entity-id/enabled-for-signin"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("[\"https://some-idp-entity-id\"]"))
        );

        var response = selectIdp("https://some-idp-entity-id", "LEVEL_1", false, "some-session-id");
        assertThat(response.getStatus()).isEqualTo(400);
        var error = response.readEntity(new GenericType<Map<String, String>>() { });
        assertThat(error.get("exceptionType")).isEqualTo("STATE_PROCESSING_VALIDATION");
        assertThat(error.get("clientMessage")).isEqualTo("Invalid transition 'selectIdp' for state 'match'");
    }

    @Ignore
    @Test
    public void tryAnotherIdpShouldReturnSuccess() {
        throw new NotImplementedException("Test tryAnotherIdpShouldReturnSuccess has not been implemented");
    }

    @Ignore
    @Test
    public void getSignInProcessDtoShouldReturnSignInDetailsDto() {
        throw new NotImplementedException("Test getSignInProcessDtoShouldReturnSignInDetailsDto has not been implemented");
    }

    @Test
    public void getRequestIssuerIdShouldReturnRequestIssuerId() {
        redisClient.set("state:some-session-id", VerifySessionState.IdpSelected.NAME);
        redisClient.hset("session:some-session-id", "issuer", "https://some-service-entity-id");
        Response response = getRequestIssuerId("some-session-id");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("https://some-service-entity-id");
    }

    private Response selectIdp(String selectedIdpEntityId, String requestedLevelOfAssurance, boolean isRegistration, String sessionId) {
        var url = String.format("http://localhost:%d/policy/received-authn-request/%s/select-identity-provider", verifyControl.getLocalPort(), sessionId);
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(
                mapOf(
                    "selectedIdpEntityId", selectedIdpEntityId,
                    "principalIpAddress", "8.8.8.8",
                    "registration", isRegistration,
                    "requestedLoa", requestedLevelOfAssurance
                    ),
                MediaType.APPLICATION_JSON_TYPE)
            );
    }

    private Response getRequestIssuerId(String sessionId) {
        var url = String.format("http://localhost:%d/policy/received-authn-request/%s/registration-request-issuer-id", verifyControl.getLocalPort(), sessionId);
        return httpClient.target(url).request(MediaType.APPLICATION_JSON_TYPE).get();
    }
}
