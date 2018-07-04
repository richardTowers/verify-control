package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;

import javax.ws.rs.core.Response;

public class MatchResponseHandler {
    public static Response handleMatchResponse(SessionClient sessionClient, String sessionId) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        sessionClient.setState(sessionId, state.match());
        return Response.ok().build();
    }
}
