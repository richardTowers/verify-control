package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.api.AuthnRequest;
import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlEngineClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static java.lang.Boolean.parseBoolean;
import static javax.ws.rs.core.Response.created;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateTime;
import static uk.gov.ida.hub.control.handlers.AuthenticationFailedHandler.handleAuthenticationFailed;
import static uk.gov.ida.hub.control.handlers.AuthenticationSuccessHandler.handleAuthenticationSuccess;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

@Path("/policy/session")
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    private final SessionClient sessionClient;
    private final SamlEngineClient samlEngineClient;
    private final ConfigServiceClient configServiceClient;
    private final SamlSoapProxyClient samlSoapProxyClient;

    public SessionResource(
        SessionClient sessionClient,
        SamlEngineClient samlEngineClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient
    ) {
        this.sessionClient = sessionClient;
        this.samlEngineClient = samlEngineClient;
        this.configServiceClient = configServiceClient;
        this.samlSoapProxyClient = samlSoapProxyClient;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    public Response createSession(@Valid @NotNull AuthnRequest authnRequest) {
        var result = samlEngineClient.translateRpAuthnRequest(authnRequest.getSamlRequest());
        var sessionId = sessionClient.initialise(mapOf(
            "start", now(UTC).toString(dateTime()),
            "issuer", result.get("issuer"),
            "requestId", result.get("requestId"),
            "relayState", authnRequest.getRelayState(),
            "ipAddress", authnRequest.getPrincipalIPAddressAsSeenByHub()
        ));
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
        String selectedIdp = sessionClient.get(sessionId, "selectedIdp");

        var authnRequest = samlEngineClient.generateIdpAuthnRequest(selectedIdp);

        return Response.ok().entity(mapOf(
            "samlRequest", authnRequest.getSamlRequest(),
            "postEndpoint", authnRequest.getSsoUri(),
            "registering", parseBoolean(sessionClient.get(sessionId, "isRegistration"))
        )).build();
    }

    @POST
    @Path("/{sessionId}/idp-authn-response")
    public Response receiveIdpAuthnResponse(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) {
        var issuer = sessionClient.get(sessionId, "issuer");
        var matchingServiceEntityId = configServiceClient.getMatchingServiceEntityId(issuer);

        var samlEngineResponse = samlEngineClient.translateIdpResponse(
            sessionId,
            samlResponse.get("samlResponse"),
            samlResponse.get("principalIPAddressAsSeenByHub"),
            matchingServiceEntityId
        );

        var status = samlEngineResponse.get("status");

        switch (status) {
            case "NoAuthenticationContext":
            case "AuthenticationFailed":
                return handleAuthenticationFailed(sessionClient, sessionId);
            case "Success": {
                return handleAuthenticationSuccess(
                    sessionClient,
                    configServiceClient,
                    samlSoapProxyClient,
                    sessionId,
                    matchingServiceEntityId,
                    samlEngineResponse
                );
            }
            default:
                throw new NotImplementedException("Status '" + status + "' has not been implemented");
        }
    }
}
