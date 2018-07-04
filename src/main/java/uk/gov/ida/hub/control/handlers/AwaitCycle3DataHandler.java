package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.Response;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class AwaitCycle3DataHandler {
    public static Response handleAwaitCycle3Data(
        SessionClient sessionClient,
        String sessionId
    ) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        var stateToTransitionTo = state.awaitCycle3Data();

        return handleAwaitCycle3DataAndTransition(sessionClient, sessionId, stateToTransitionTo);
    }

    private static Response handleAwaitCycle3DataAndTransition(
        SessionClient sessionClient,
        String sessionId,
        VerifySessionState.AwaitingCycle3Data stateToTransitionTo
    ) throws SessionNotFoundException {
        sessionClient.setState(sessionId, stateToTransitionTo);
        return Response.ok(mapOf(
            "responseProcessingStatus", "GET_C3_DATA",
            "sessionId", sessionId
        )).build();
    }
}
