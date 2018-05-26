package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import redis.clients.jedis.Jedis;
import uk.gov.ida.hub.control.configuration.RedisConfiguration;

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

    private final Jedis redisClient;

    public SessionResource(RedisConfiguration redisConfiguration) {
        this.redisClient = new Jedis(redisConfiguration.getHost(), redisConfiguration.getPort());
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
