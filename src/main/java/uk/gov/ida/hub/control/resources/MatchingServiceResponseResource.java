package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.clients.SamlEngineClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.ApiBadRequestException;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static uk.gov.ida.hub.control.handlers.AwaitCycle3DataHandler.handleAwaitCycle3Data;
import static uk.gov.ida.hub.control.handlers.MatchResponseHandler.handleMatchResponse;

@Path("/policy/session/{sessionId}/attribute-query-response")
@Consumes(MediaType.APPLICATION_JSON)
public class MatchingServiceResponseResource {
    private final SessionClient sessionClient;
    private final SamlEngineClient samlEngineClient;

    public MatchingServiceResponseResource(
        SessionClient sessionClient,
        SamlEngineClient samlEngineClient
    ) {
        this.sessionClient = sessionClient;
        this.samlEngineClient = samlEngineClient;
    }

    @POST
    @Timed
    public Response receiveAttributeQueryResponseFromMatchingService(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) throws SessionNotFoundException, ApiBadRequestException {
        var session = sessionClient.getAll(sessionId);
        var translatedResponse = samlEngineClient.translateMatchingServiceResponse(samlResponse.get("samlResponse"));
        var status = translatedResponse.get("status");
        switch (status) {
            case "MatchingServiceMatch":
                return handleMatchResponse(sessionClient, sessionId);
            case "NoMatchingServiceMatchFromMatchingService":
                // TODO ... if cycle 3 is configured:
                return handleAwaitCycle3Data(sessionClient, sessionId);
            // TODO: other cases
            default:
                throw new NotImplementedException("Unknown matching status: " + status);
        }
    }
}
