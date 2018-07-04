package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.api.AuthnRequest;
import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.dtos.samlengine.SamlRequestDto;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static java.lang.Boolean.parseBoolean;
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
    private final WebTarget samlEngineWebTarget;
    private final ConfigServiceClient configServiceClient;
    private final SamlSoapProxyClient samlSoapProxyClient;

    public SessionResource(
        RedisCommands<String, String> redisClient,
        WebTarget samlEngineWebTarget,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient
    ) {
        this.redisClient = redisClient;
        this.samlEngineWebTarget = samlEngineWebTarget;
        this.configServiceClient = configServiceClient;
        this.samlSoapProxyClient = samlSoapProxyClient;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response createSession(@Valid @NotNull AuthnRequest authnRequest) {
        var sessionId = UUID.randomUUID().toString();

        var result = samlEngineWebTarget
            .path("/saml-engine/translate-rp-authn-request")
            .request(APPLICATION_JSON_TYPE)
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

    @GET
    @Path("/{sessionId}/idp-authn-request-from-hub")
    public Response getIdpAuthnRequestFromHub(@PathParam("sessionId") String sessionId) {
        String selectedIdp = redisClient.hget("session:" + sessionId, "selectedIdp");

        var parameters = ImmutableMap.builder()
            .put("idpEntityId", selectedIdp)
            .put("levelsOfAssurance", ImmutableList.of("LEVEL_1")) // TODO get this from config
            .build();

        var samlEngineResponse = samlEngineWebTarget
            .path("/saml-engine/generate-idp-authn-request")
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(parameters, APPLICATION_JSON_TYPE))
            .invoke(SamlRequestDto.class);

        return Response.ok().entity(mapOf(
            "samlRequest", samlEngineResponse.getSamlRequest(),
            "postEndpoint", samlEngineResponse.getSsoUri(),
            "registering", parseBoolean(redisClient.hget("session:" + sessionId, "isRegistration"))
        )).build();
    }

    @POST
    @Path("/{sessionId}/idp-authn-response")
    public Response receiveIdpAuthnResponse(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) {
        var session = redisClient.hgetall("session:" + sessionId);
        var originalState = VerifySessionState.forName(redisClient.get("state:" + sessionId));
        var issuer = session.get("issuer");

        var matchingServiceEntityId = configServiceClient.getMatchingServiceEntityId(issuer);

        var samlEngineResponse = samlEngineWebTarget
            .path("/saml-engine/translate-idp-authn-response")
            .request(APPLICATION_JSON_TYPE)
            .buildPost(entity(mapOf(
                "samlResponse", samlResponse.get("samlResponse"),
                "sessionId", sessionId,
                "principalIPAddressAsSeenByHub", samlResponse.get("principalIPAddressAsSeenByHub"),
                "matchingServiceEntityId", matchingServiceEntityId
            ), APPLICATION_JSON_TYPE))
            .invoke()
            .readEntity(new GenericType<Map<String, String>>() {{}});

        var status = samlEngineResponse.get("status");

        switch (status) {
            case "AuthenticationFailed": {
                var newState = originalState.authenticationFailed();
                redisClient.set("state:" + sessionId, newState.getName());
                return Response.ok(mapOf(
                    "sessionId", sessionId,
                    "result", "OTHER",
                    "isRegistration", parseBoolean(session.get("isRegistration"))
                )).build();
            }
            case "Success": {
                var newState = originalState.authenticationSucceeded();

                var matchingServiceConfig = configServiceClient.getMatchingServiceConfig(matchingServiceEntityId);
                samlSoapProxyClient.makeMatchingServiceRequest(
                    sessionId,
                    session.get("requestId"),
                    samlEngineResponse.get("encryptedMatchingDatasetAssertion"),
                    samlEngineResponse.get("authnStatementAssertion"),
                    matchingServiceEntityId,
                    samlEngineResponse.get("loaAchieved"),
                    samlEngineResponse.get("persistentId"),
                    matchingServiceConfig.get("uri"),
                    issuer,
                    matchingServiceConfig.get("onboarding")
                );

                redisClient.set("state:" + sessionId, newState.getName());
                return Response.ok(mapOf(
                    "sessionId", sessionId,
                    "result", "SUCCESS",
                    "isRegistration", parseBoolean(session.get("isRegistration")),
                    "loaAchieved", samlEngineResponse.get("loaAchieved")
                ), APPLICATION_JSON_TYPE).build();
            }
            default:
                throw new NotImplementedException("Status '" + status + "' has not been implemented");
        }
    }
}
