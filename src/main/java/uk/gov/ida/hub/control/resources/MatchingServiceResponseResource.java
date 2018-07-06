package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.NotImplementedException;
import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlEngineClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.ApiBadRequestException;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static uk.gov.ida.hub.control.handlers.AwaitCycle3DataHandler.handleAwaitCycle3Data;
import static uk.gov.ida.hub.control.handlers.MatchResponseHandler.handleMatchResponse;
import static uk.gov.ida.hub.control.handlers.UserAccountCreationHandler.handleUserAccountCreated;
import static uk.gov.ida.hub.control.handlers.UserAccountCreationHandler.handleUserAccountCreationRequest;

@Path("/policy/session/{sessionId}/attribute-query-response")
@Consumes(MediaType.APPLICATION_JSON)
public class MatchingServiceResponseResource {
    private final SessionClient sessionClient;
    private final SamlEngineClient samlEngineClient;
    private final ConfigServiceClient configServiceClient;
    private final SamlSoapProxyClient samlSoapProxyClient;

    public MatchingServiceResponseResource(
        SessionClient sessionClient,
        SamlEngineClient samlEngineClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient) {
        this.sessionClient = sessionClient;
        this.samlEngineClient = samlEngineClient;
        this.configServiceClient = configServiceClient;
        this.samlSoapProxyClient = samlSoapProxyClient;
    }

    @POST
    @Timed
    public Response receiveAttributeQueryResponseFromMatchingService(@PathParam("sessionId") String sessionId, Map<String, String> samlResponse) throws SessionNotFoundException, ApiBadRequestException {
        var state = sessionClient.getState(sessionId);
        var issuer = sessionClient.get(sessionId, "issuer");
        var translatedResponse = samlEngineClient.translateMatchingServiceResponse(samlResponse.get("samlResponse"));
        var status = translatedResponse.get("status");
        switch (status) {
            case "MatchingServiceMatch":
                return handleMatchResponse(sessionClient, sessionId);
            case "NoMatchingServiceMatchFromMatchingService":
                var matchingStage = state.getMatchingStage();
                switch (matchingStage) {
                    case CYCLE_0_AND_1:
                        if (configServiceClient.getCycle3AttributeName(issuer) != null) {
                            return handleAwaitCycle3Data(sessionClient, sessionId);
                        }
                        else {
                            if (true) { // TODO if user account creation is enabled
                                return handleUserAccountCreationRequest(sessionClient, configServiceClient, samlSoapProxyClient, sessionId);
                            }
                            else {
                                throw new NotImplementedException("No match for cycle 0 and 1 - user account creation disabled"); // TODO
                            }
                        }
                    case CYCLE_3:
                        if (true) { // TODO if user account creation is enabled
                            return handleUserAccountCreationRequest(sessionClient, configServiceClient, samlSoapProxyClient, sessionId);
                        }
                        else {
                            throw new NotImplementedException("No match for cycle 3 - user account creation disabled"); // TODO
                        }
                    default:
                        throw new NotImplementedException("Matching stage " + matchingStage + " has not been implemented");
                }
            case "UserAccountCreated":
                return handleUserAccountCreated(sessionClient, sessionId);
            default:
                throw new NotImplementedException("Unknown matching status: " + status);
        }
    }
}
