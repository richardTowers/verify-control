package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.joda.time.DateTime;
import uk.gov.ida.hub.control.api.AuthnRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateTime;

@Path("/policy/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    private final RedisCommands<String, String> redisClient;

    public SessionResource(String redisUrl) {
        this.redisClient = RedisClient
            .create(redisUrl)
            .connect().sync();
    }

    @POST
    @Consumes
    @Timed
    public Response createSession(@Valid @NotNull AuthnRequest authnRequest) {
        var sessionId = UUID.randomUUID().toString();

        // TODO unpack the SAML request by calling saml engine

        var session = ImmutableMap.of(
            "start", DateTime.now(UTC).toString(dateTime()),
            "issuer", "TODO: issuer",
            "requestId", "TODO: requestId",
            "relayState", authnRequest.getRelayState(),
            "ipAddress", authnRequest.getPrincipalIPAddressAsSeenByHub()
        );
        redisClient.hmset("session:" + sessionId, session);

        return Response.created(null).entity(sessionId).build();
    }
}
