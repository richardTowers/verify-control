package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.ida.hub.control.clients.SessionClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

@Path("/policy/session/{sessionId}/attribute-query-response")
@Consumes(MediaType.APPLICATION_JSON)
public class MatchingServiceResponseResource {
    private final SessionClient sessionClient;

    public MatchingServiceResponseResource(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    @POST
    @Timed
    public Response receiveAttributeQueryResponseFromMatchingService(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) {
        var state = sessionClient.getState(sessionId);
        // TODO ... if no match and cycle 3 is configured:
        var newState = state.awaitCycle3Data();
        sessionClient.setState(sessionId, newState);
        return Response.ok(mapOf(
            "responseProcessingStatus", "GET_C3_DATA",
            "sessionId", sessionId
        )).build();
    }
}
