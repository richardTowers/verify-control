package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.Response;

import static java.lang.Boolean.parseBoolean;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class FraudResponseHandler {
    public static Response handleFraudResponse(SessionClient sessionClient, String sessionId) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        VerifySessionState.FraudResponse stateToTransitionTo = state.fraudResponse();
        var isRegistration = parseBoolean(sessionClient.get(sessionId, "isRegistration"));
        return handleFraudResponseAndTransition(sessionClient, sessionId, isRegistration, stateToTransitionTo);
    }

    private static Response handleFraudResponseAndTransition(
        SessionClient sessionClient,
        String sessionId,
        boolean isRegistration,
        VerifySessionState stateToTransitionTo
    ) throws SessionNotFoundException {
        sessionClient.setState(sessionId, stateToTransitionTo);
        return Response.ok(mapOf(
            "sessionId", sessionId,
            "result", "OTHER",
            "isRegistration", isRegistration
        )).build();
    }
}
