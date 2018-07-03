package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import io.lettuce.core.api.sync.RedisCommands;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Map;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

@Path("/policy/received-authn-request/{sessionId}/cycle-3-attribute")
@Consumes(MediaType.APPLICATION_JSON)
public class Cycle3DataResource {
    private final RedisCommands<String, String> redisClient;
    private final WebTarget configServiceTarget;
    private final WebTarget samlSoapProxyWebTarget;

    public Cycle3DataResource(
        RedisCommands<String, String> redisClient,
        WebTarget configServiceTarget,
        WebTarget samlSoapProxyWebTarget
    ) {
        this.redisClient = redisClient;
        this.configServiceTarget = configServiceTarget;
        this.samlSoapProxyWebTarget = samlSoapProxyWebTarget;
    }

    @POST
    @Path("/submit")
    @Timed
    public void submitCycle3Data(@PathParam("sessionId") String sessionId, Map<String, String> cycle3UserInput) {
        var stateKey = "state:" + sessionId;
        var session = redisClient.hgetall("session:" + sessionId);

        var samlSoapProxyRequest = ImmutableMap.builder()
            .put("id", session.get("requestId"))
            .put("issuer", session.get("issuer"))
            .put("samlRequest", "TODO") // TODO
            .put("matchingServiceUri", "TODO") // TODO
            .put("assertionConsumerServiceUri", "https://todo_get_this_from_config") // TODO get this from config
            .put("attributeQueryClientTimeOut", DateTime.now().plusMinutes(5)) // TODO get this from config
            .put("onboarding", "TODO") // TODO
            .build();

        int samlSoapProxyResponseStatus = samlSoapProxyWebTarget
            .path("/matching-service-request-sender")
            .queryParam("sessionId", sessionId)
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(samlSoapProxyRequest, APPLICATION_JSON_TYPE))
            .invoke()
            .getStatus();

        if (samlSoapProxyResponseStatus != 200) {
            throw new RuntimeException("TODO: better exception"); // TODO better exception
        }
        VerifySessionState state = VerifySessionState.forName(redisClient.get(stateKey));
        redisClient.set(stateKey, state.submitCycle3Request().getName());
    }
}
