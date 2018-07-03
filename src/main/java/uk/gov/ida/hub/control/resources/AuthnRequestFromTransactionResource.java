package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.dtos.SelectIdpDto;
import uk.gov.ida.hub.control.errors.EntityNotEnabledException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/policy/received-authn-request")
public class AuthnRequestFromTransactionResource {
    private final RedisCommands<String, String> redisClient;
    private final ConfigServiceClient configServiceClient;

    public AuthnRequestFromTransactionResource(RedisCommands<String, String> redisClient, ConfigServiceClient configServiceClient) {
        this.redisClient = redisClient;
        this.configServiceClient = configServiceClient;
    }

    @POST
    @Path("/{sessionId}/select-identity-provider")
    @Timed
    public Response selectIdentityProvider(@PathParam("sessionId") String sessionId, @Valid SelectIdpDto selectIdpDto) {
        var issuer = redisClient.hget("session:" + sessionId, "issuer");

        var enabledIdps = configServiceClient
            .getEnabledIdps(issuer, selectIdpDto.getRequestedLoa(), selectIdpDto.isRegistration());

        var originalState = VerifySessionState.forName(redisClient.get("state:" + sessionId));
        if (!enabledIdps.contains(selectIdpDto.getSelectedIdpEntityId())) {
            throw new EntityNotEnabledException(selectIdpDto.getSelectedIdpEntityId(), originalState);
        }
        redisClient.hset("session:" + sessionId, "isRegistration", Boolean.toString(selectIdpDto.isRegistration()));
        redisClient.hset("session:" + sessionId, "selectedIdp", selectIdpDto.getSelectedIdpEntityId());
        var newState = originalState.selectIdp();
        redisClient.set("state:" + sessionId, newState.getName());
        return Response.status(201).build();
    }

    /**
     * From a brief look at Policy it looks like the whole "AuthnFailed -> SessionStarted" transition
     * is unnecessary - both states permit the same set of actions, so we may as well not transition
     * and have this as a no-op.
     */
    @POST
    @Path("/{sessionId}/try-another-idp")
    @Timed
    public void tryAnotherIdp(@PathParam("sessionId") String sessionId) {
        // Deliberate no-op
    }

    @GET
    @Path("/{sessionId}/sign-in-process-details")
    @Timed
    public Map<String, Object> getSignInProcessDto(@PathParam("sessionId") String sessionId) {
        var requestIssuerId = redisClient.hget("session:" + sessionId, "issuer");
        var eidasEnabled = configServiceClient.isEidasEnabled(requestIssuerId);
        return new HashMap<>() {{
            put("requestIssuerId", requestIssuerId);
            put("transactionSupportsEidas", eidasEnabled);
        }};
    }

    @GET
    @Path("/{sessionId}/registration-request-issuer-id")
    @Timed
    public String getRequestIssuerId(@PathParam("sessionId") String sessionId) {
        return redisClient.hget("session:" + sessionId, "issuer");
    }
}
