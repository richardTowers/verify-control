package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static uk.gov.ida.hub.control.handlers.AwaitCycle3DataHandler.handleAwaitCycle3Data;

@Path("/policy/session/{sessionId}/attribute-query-response")
@Consumes(MediaType.APPLICATION_JSON)
public class MatchingServiceResponseResource {
    private final SessionClient sessionClient;

    public MatchingServiceResponseResource(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    @POST
    @Timed
    public Response receiveAttributeQueryResponseFromMatchingService(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) throws SessionNotFoundException {
        switch ("TODO") {
            // TODO ... if no match and cycle 3 is configured:
            case "TODO":
                return handleAwaitCycle3Data(sessionClient, sessionId);
            // TODO: other cases
            default:
                throw new NotImplementedException("Don't know how to handle this matching result");
        }
    }
}
