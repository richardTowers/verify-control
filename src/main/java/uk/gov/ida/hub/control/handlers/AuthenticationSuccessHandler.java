package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.Response;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class AuthenticationSuccessHandler {
    public static Response handleAuthenticationSuccess(
        SessionClient sessionClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient,
        String sessionId,
        String matchingServiceEntityId,
        Map<String, String> samlEngineResponse
    ) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        var stateToTransitionTo = state.authenticationSucceeded();

        return handleAuthenticationSuccessAndTransition(
            sessionClient,
            configServiceClient,
            samlSoapProxyClient,
            sessionId,
            matchingServiceEntityId,
            samlEngineResponse,
            stateToTransitionTo
        );
    }

    private static Response handleAuthenticationSuccessAndTransition(
        SessionClient sessionClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient,
        String sessionId,
        String matchingServiceEntityId,
        Map<String, String> samlEngineResponse,
        VerifySessionState.Matching stateToTransitionTo
    ) throws SessionNotFoundException {
        var session = sessionClient.getAll(sessionId);

        var matchingServiceConfig = configServiceClient.getMatchingServiceConfig(matchingServiceEntityId);
        samlSoapProxyClient.makeMatchingServiceRequest(
            sessionId,
            session.get("requestId"),
            samlEngineResponse.get("encryptedMatchingDatasetAssertion"),
            samlEngineResponse.get("authnStatementAssertion"),
            matchingServiceEntityId,
            samlEngineResponse.get("loaAchieved"),
            samlEngineResponse.get("persistentId"),
            matchingServiceConfig.get("uri"),
            sessionClient.get(sessionId, "issuer"),
            matchingServiceConfig.get("onboarding")
        );

        sessionClient.setState(sessionId, stateToTransitionTo);
        return Response.ok(mapOf(
            "sessionId", sessionId,
            "result", "SUCCESS",
            "isRegistration", parseBoolean(session.get("isRegistration")),
            "loaAchieved", samlEngineResponse.get("loaAchieved")
        ), APPLICATION_JSON_TYPE).build();
    }
}
