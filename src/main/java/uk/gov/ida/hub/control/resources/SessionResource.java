package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.api.AuthnRequest;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.created;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateTime;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

@Path("/policy/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    private final RedisCommands<String, String> redisClient;
    private final Invocation.Builder samlEngineInvocationBuilder;

    public SessionResource(String redisUrl, Invocation.Builder samlEngineInvocationBuilder) {
        this.redisClient = RedisClient
            .create(redisUrl)
            .connect().sync();
        this.samlEngineInvocationBuilder = samlEngineInvocationBuilder;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response createSession(@Valid @NotNull AuthnRequest authnRequest) {
        var sessionId = UUID.randomUUID().toString();

        var result = samlEngineInvocationBuilder
            .buildPost(entity(mapOf("samlMessage", authnRequest.getSamlRequest()), APPLICATION_JSON_TYPE))
            .invoke(new GenericType<Map<String, String>>() {});

        var session = mapOf(
            "start", now(UTC).toString(dateTime()),
            "issuer", result.get("issuer"),
            "requestId", result.get("requestId"),
            "relayState", authnRequest.getRelayState(),
            "ipAddress", authnRequest.getPrincipalIPAddressAsSeenByHub()
        );
        redisClient.hmset("session:" + sessionId, session);

        return created(null).entity(sessionId).build();
    }

    @GET
    @Path("/{sessionId}")
    public Response getSession(@PathParam("sessionId") @NotNull String sessionId) throws SessionNotFoundException {
        throw new SessionNotFoundException(sessionId);
    }
}
