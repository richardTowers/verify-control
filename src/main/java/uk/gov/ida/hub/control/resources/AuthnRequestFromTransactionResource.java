package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.dtos.SelectIdpDto;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/policy/received-authn-request")
public class AuthnRequestFromTransactionResource {
    private final RedisCommands<String, String> redisClient;

    public AuthnRequestFromTransactionResource(RedisCommands<String, String> redisClient) {
        this.redisClient = redisClient;
    }

    @POST
    @Path("/{sessionId}/select-identity-provider")
    @Timed
    public Response selectIdentityProvider(@PathParam("sessionId") String sessionId, @Valid SelectIdpDto selectIdpDto) {
        var originalState = VerifySessionState.forName(redisClient.get("state:" + sessionId));
        var newState = originalState.selectIdp();
        redisClient.set("state:" + sessionId, newState.getName());
        return Response.status(201).build();
    }
}
