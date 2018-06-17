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
        var response = selectIdp("some-session-id", "LEVEL_1", false);
        assertThat(response.getStatus()).isEqualTo(201);
        // TODO: (event sink) This test should check that event sink was called.
    }

    @Ignore
    @Test
    public void idpSelectedShouldThrowIfIdpIsNotAvailable() {
        throw new NotImplementedException("Test idpSelectedShouldThrowIfIdpIsNotAvailable has not been implemented");
    }

    @Test
    public void idpSelectedShouldThrowIfSessionInWrongState() {
        redisClient.set("state:some-session-id", VerifySessionState.Match.NAME);
        var response = selectIdp("some-session-id", "LEVEL_1", false);
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

    @Ignore
    @Test
    public void getRequestIssuerIdShouldReturnRequestIssuerId() {
        throw new NotImplementedException("Test getRequestIssuerIdShouldReturnRequestIssuerId has not been implemented");
    }

    private Response selectIdp(String sessionId, String requestedLevelOfAssurance, boolean isRegistration) {
        var url = String.format("http://localhost:%d/policy/received-authn-request/%s/select-identity-provider", verifyControl.getLocalPort(), sessionId);
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(
                mapOf(
                    "selectedIdpEntityId", "https://some-idp-entity-id",
                    "principalIpAddress", "8.8.8.8",
                    "registration", isRegistration,
                    "requestedLoa", requestedLevelOfAssurance
                    ),
                MediaType.APPLICATION_JSON_TYPE)
            );
    }
}
