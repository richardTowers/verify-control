package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

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
    public Response createSession() {
        var sessionId = UUID.randomUUID().toString();
        redisClient.append("session:" + sessionId, "banana");
        return Response.created(null).entity(sessionId).build();
    }
}
