package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

@Path("/policy/received-authn-request/{sessionId}/response-from-idp")
@Consumes(MediaType.APPLICATION_JSON)
public class ResponseFromIdpResource {
    private final SessionClient sessionClient;

    public ResponseFromIdpResource(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    @GET
    @Path("/response-processing-details")
    @Timed
    public Response getResponseProcessingDetails(@PathParam("sessionId") String sessionId) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        var issuer = sessionClient.get(sessionId, "issuer");
        switch (state.getName()) {
            case VerifySessionState.UserAccountCreated.NAME:
                return Response.ok(mapOf(
                    "sessionId", sessionId,
                    "responseProcessingStatus", "SEND_USER_ACCOUNT_CREATED_RESPONSE_TO_TRANSACTION",
                    "transactionEntityId", issuer
                )).build();
            default:
                throw new NotImplementedException("Can't get response processing details for state: ", state.getName());
        }
    }
}