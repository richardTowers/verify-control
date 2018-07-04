package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.Response;

import static java.lang.Boolean.parseBoolean;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class AuthenticationFailedHandler {
    public static Response handleAuthenticationFailed(
        SessionClient sessionClient,
        String sessionId
    ) {
        var state = sessionClient.getState(sessionId);
        var stateToTransitionTo = state.authenticationFailed();

        return handleAuthenticationFailedAndTransition(sessionClient, sessionId, stateToTransitionTo);
    }

    private static Response handleAuthenticationFailedAndTransition(
        SessionClient sessionClient,
        String sessionId,
        VerifySessionState stateToTransitionTo
    ) {
        var isRegistration = parseBoolean(sessionClient.get(sessionId, "isRegistration"));
        sessionClient.setState(sessionId, stateToTransitionTo);
        return Response.ok(mapOf(
            "sessionId", sessionId,
            "result", "OTHER",
            "isRegistration", isRegistration
        )).build();
    }
}
