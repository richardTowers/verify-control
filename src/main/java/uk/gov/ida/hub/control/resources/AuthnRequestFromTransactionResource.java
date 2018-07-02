package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.dtos.SelectIdpDto;
import uk.gov.ida.hub.control.errors.EntityNotEnabledException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/policy/received-authn-request")
public class AuthnRequestFromTransactionResource {
    private final RedisCommands<String, String> redisClient;
    private final WebTarget configServiceTarget;

    public AuthnRequestFromTransactionResource(RedisCommands<String, String> redisClient, WebTarget configServiceTarget) {
        this.redisClient = redisClient;
        this.configServiceTarget = configServiceTarget;
    }

    @POST
    @Path("/{sessionId}/select-identity-provider")
    @Timed
    public Response selectIdentityProvider(@PathParam("sessionId") String sessionId, @Valid SelectIdpDto selectIdpDto) {
        var issuer = redisClient.hget("session:" + sessionId, "issuer");
        var configTarget = selectIdpDto.isRegistration()
            ? configServiceTarget.path("/config/idps/{entityId}/{levelOfAssurance}/enabled")
                .resolveTemplate("entityId", issuer)
                .resolveTemplate("levelOfAssurance", selectIdpDto.getRequestedLoa())
            : configServiceTarget.path("/config/idps/{entityId}/enabled-for-signin")
                .resolveTemplate("entityId", issuer);

        var response = configTarget
            .request(APPLICATION_JSON_TYPE)
            .buildGet()
            .invoke()
            .readEntity(new GenericType<List<String>>() { });

        var originalState = VerifySessionState.forName(redisClient.get("state:" + sessionId));
        if (!response.contains(selectIdpDto.getSelectedIdpEntityId())) {
            throw new EntityNotEnabledException(selectIdpDto.getSelectedIdpEntityId(), originalState);
        }
        var newState = originalState.selectIdp();
        redisClient.set("state:" + sessionId, newState.getName());
        return Response.status(201).build();
    }

    @GET
    @Path("/{sessionId}/registration-request-issuer-id")
    @Timed
    public String getRequestIssuerId(@PathParam("sessionId") String sessionId) {
        return redisClient.hget("session:" + sessionId, "issuer");
    }
}
