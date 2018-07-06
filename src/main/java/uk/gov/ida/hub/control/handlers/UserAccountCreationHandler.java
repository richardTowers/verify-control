package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.Response;

public class UserAccountCreationHandler {
    public static Response handleUserAccountCreationRequest(
        SessionClient sessionClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient,
        String sessionId
    ) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        var newState = state.sendUserAccountCreationRequest();
        return handleUserAccountCreationRequestAndTransition(sessionClient, configServiceClient, samlSoapProxyClient, sessionId, newState);
    }

    private static Response handleUserAccountCreationRequestAndTransition(
        SessionClient sessionClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient, String sessionId,
        VerifySessionState stateToTransitionTo
    ) throws SessionNotFoundException {
        var session = sessionClient.getAll(sessionId);
        var issuer = session.get("issuer");
        var matchingServiceEntityId = configServiceClient.getMatchingServiceEntityId(issuer);
        var userAccountCreationAttributes = configServiceClient.getUserAccountCreationAttributes(matchingServiceEntityId);
        var matchingServiceConfig = configServiceClient.getMatchingServiceConfig(matchingServiceEntityId);

        samlSoapProxyClient.makeMatchingServiceRequest(
            sessionId,
            session.get("requestId"),
            session.get("encryptedMatchingDatasetAssertion"),
            session.get("authnStatementAssertion"),
            matchingServiceEntityId,
            session.get("loaAchieved"),
            session.get("persistentId"),
            matchingServiceConfig.get("uri"),
            sessionClient.get(sessionId, "issuer"),
            matchingServiceConfig.get("onboarding"),
            userAccountCreationAttributes
        );

        sessionClient.setState(sessionId, stateToTransitionTo);
        return Response.ok().build();
    }

    public static Response handleUserAccountCreated(SessionClient sessionClient, String sessionId) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        var newState = state.userAccountCreationSucceeded();

        // TODO do we need any side effects here?

        sessionClient.setState(sessionId, newState);
        return Response.ok().build();
    }

    public static Response handleUserAccountCreationFailed(SessionClient sessionClient, String sessionId) throws SessionNotFoundException {
        var state = sessionClient.getState(sessionId);
        var newState = state.userAccountCreationFailed();

        // TODO do we need any side effects here?

        sessionClient.setState(sessionId, newState);
        return Response.ok().build();
    }
}
