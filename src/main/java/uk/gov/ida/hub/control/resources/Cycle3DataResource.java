package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
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
    private final RedisCommands<String, String> redisClient;
    private final SamlSoapProxyClient samlSoapProxyClient;

    public Cycle3DataResource(
        RedisCommands<String, String> redisClient,
        SamlSoapProxyClient samlSoapProxyClient
    ) {
        this.redisClient = redisClient;
        this.samlSoapProxyClient = samlSoapProxyClient;
    }

    @POST
    @Path("/submit")
    @Timed
    public void submitCycle3Data(@PathParam("sessionId") String sessionId, Map<String, String> cycle3UserInput) {
        var stateKey = "state:" + sessionId;
        var session = redisClient.hgetall("session:" + sessionId);

        samlSoapProxyClient.makeCycle3MatchingServiceRequest(sessionId, session.get("requestId"), session.get("issuer"));

        VerifySessionState state = VerifySessionState.forName(redisClient.get(stateKey));
        redisClient.set(stateKey, state.submitCycle3Request().getName());
    }
}
