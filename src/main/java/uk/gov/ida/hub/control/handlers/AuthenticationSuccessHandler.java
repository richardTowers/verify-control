package uk.gov.ida.hub.control.handlers;

import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.clients.SessionClient;
import uk.gov.ida.hub.control.dtos.LevelOfAssurance;
import uk.gov.ida.hub.control.errors.ConditionNotMetException;
import uk.gov.ida.hub.control.errors.IdpDisabledException;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Boolean.parseBoolean;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class AuthenticationSuccessHandler {
    public static Response handleAuthenticationSuccess(
        SessionClient sessionClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient,
        String sessionId,
        String matchingServiceEntityId,
        Map<String, String> samlEngineResponse
    ) throws SessionNotFoundException, ConditionNotMetException, IdpDisabledException {
        var state = sessionClient.getState(sessionId);
        var stateToTransitionTo = state.authenticationSucceeded();

        return handleAuthenticationSuccessAndTransition(
            sessionClient,
            configServiceClient,
            samlSoapProxyClient,
            sessionId,
            matchingServiceEntityId,
            samlEngineResponse,
            stateToTransitionTo
        );
    }

    private static Response handleAuthenticationSuccessAndTransition(
        SessionClient sessionClient,
        ConfigServiceClient configServiceClient,
        SamlSoapProxyClient samlSoapProxyClient,
        String sessionId,
        String matchingServiceEntityId,
        Map<String, String> samlEngineResponse,
        VerifySessionState.Matching stateToTransitionTo
    ) throws SessionNotFoundException, ConditionNotMetException, IdpDisabledException {
        var session = sessionClient.getAll(sessionId);
        var allowedLevelsOfAssurance = configServiceClient.getLevelsOfAssurance(session.get("issuer"));

        var loaAchieved = samlEngineResponse.get("loaAchieved");
        var levelOfAssuranceAchieved = LevelOfAssurance.valueOf(loaAchieved);
        if (!allowedLevelsOfAssurance.contains(levelOfAssuranceAchieved)) {
            throw new ConditionNotMetException(
                "Level of Assurance not permitted. Needed one of [" +
                    String.join(", ", allowedLevelsOfAssurance.stream().map(Enum::toString).collect(Collectors.toList()))
                    + "] but got " +
                    levelOfAssuranceAchieved
            );
        }
        var enabledIdps = configServiceClient.getEnabledIdps(
            sessionClient.get(sessionId, "issuer"),
            levelOfAssuranceAchieved,
            parseBoolean(sessionClient.get(sessionId, "isRegistration"))
        );
        String issuer = samlEngineResponse.get("issuer");
        if (!enabledIdps.contains(issuer)) {
            throw new IdpDisabledException(issuer);
        }
        var selectedIdp = session.get("selectedIdp");
        if (!selectedIdp.equals(issuer)) {
            throw new ConditionNotMetException(
                "Issuer in response does not match selected IdP. Expected " + selectedIdp + " but got " + issuer
            );
        }


        var matchingServiceConfig = configServiceClient.getMatchingServiceConfig(matchingServiceEntityId);
        samlSoapProxyClient.makeMatchingServiceRequest(
            sessionId,
            session.get("requestId"),
            samlEngineResponse.get("encryptedMatchingDatasetAssertion"),
            samlEngineResponse.get("authnStatementAssertion"),
            matchingServiceEntityId,
            loaAchieved,
            samlEngineResponse.get("persistentId"),
            matchingServiceConfig.get("uri"),
            sessionClient.get(sessionId, "issuer"),
            matchingServiceConfig.get("onboarding")
        );

        sessionClient.setState(sessionId, stateToTransitionTo);
        return Response.ok(mapOf(
            "sessionId", sessionId,
            "result", "SUCCESS",
            "isRegistration", parseBoolean(session.get("isRegistration")),
            "loaAchieved", loaAchieved
        ), APPLICATION_JSON_TYPE).build();
    }
}
