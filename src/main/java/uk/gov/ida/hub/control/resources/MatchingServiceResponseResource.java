package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

@Path("/policy/session/{sessionId}/attribute-query-response")
@Consumes(MediaType.APPLICATION_JSON)
public class MatchingServiceResponseResource {
    private final RedisCommands<String, String> redisClient;
    private final WebTarget configServiceTarget;

    public MatchingServiceResponseResource(RedisCommands<String, String> redisClient, WebTarget configServiceTarget) {
        this.redisClient = redisClient;
        this.configServiceTarget = configServiceTarget;
    }

    @POST
    @Timed
    public Response receiveAttributeQueryResponseFromMatchingService(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) {
        var stateKey = "state:" + sessionId;
        var state = VerifySessionState.forName(redisClient.get(stateKey));
        // TODO ... if no match and cycle 3 is configured:
        var newState = state.awaitCycle3Data();
        redisClient.set(stateKey, newState.getName());
        return Response.ok(mapOf(
            "responseProcessingStatus", "GET_C3_DATA",
            "sessionId", sessionId
        )).build();
    }
}
