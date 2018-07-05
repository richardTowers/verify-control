package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/policy/received-authn-request/{sessionId}/cycle-3-attribute")
@Consumes(MediaType.APPLICATION_JSON)
public class Cycle3DataResource {
    private final SessionClient sessionClient;
    private final SamlSoapProxyClient samlSoapProxyClient;

    public Cycle3DataResource(
        SessionClient sessionClient,
        SamlSoapProxyClient samlSoapProxyClient
    ) {
        this.sessionClient = sessionClient;
        this.samlSoapProxyClient = samlSoapProxyClient;
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
        sessionClient.setState(sessionId, state.cancelCycle3Request());
    }
}
