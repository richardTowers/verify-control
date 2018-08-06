package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

@Path("/policy/received-authn-request/{sessionId}/cycle-3-attribute")
@Consumes(MediaType.APPLICATION_JSON)
public class Cycle3DataResource {
    private final SessionClient sessionClient;
    private final SamlSoapProxyClient samlSoapProxyClient;
    private final ConfigServiceClient configServiceClient;

    public Cycle3DataResource(
        SessionClient sessionClient,
        SamlSoapProxyClient samlSoapProxyClient,
        ConfigServiceClient configServiceClient) {
        this.sessionClient = sessionClient;
        this.samlSoapProxyClient = samlSoapProxyClient;
        this.configServiceClient = configServiceClient;
    }

    @GET
    public Response getCycle3AttributeDetails(@PathParam("sessionId") String sessionId) throws SessionNotFoundException {
        var issuer = sessionClient.get(sessionId, "issuer");
        return Response.ok(mapOf(
            "attributeName", configServiceClient.getCycle3AttributeName(issuer),
            "requestIssuerId", issuer
        )).build();
    }

    @POST
    @Path("/submit")
    @Timed
    public void submitCycle3Data(@PathParam("sessionId") String sessionId, Map<String, String> cycle3UserInput) throws SessionNotFoundException {
        var session = sessionClient.getAll(sessionId);
        samlSoapProxyClient.makeCycle3MatchingServiceRequest(sessionId, session.get("requestId"), session.get("issuer"));
        VerifySessionState state = sessionClient.getState(sessionId);
        sessionClient.setState(sessionId, state.submitCycle3Request());
    }

    @POST
    @Path("/cancel")
    @Timed
    public void cancelCycle3(@PathParam("sessionId") String sessionId, Map<String, String> cycle3UserInput) throws SessionNotFoundException {
        VerifySessionState state = sessionClient.getState(sessionId);
        sessionClient.setState(sessionId, state.noMatch());
    }
}
