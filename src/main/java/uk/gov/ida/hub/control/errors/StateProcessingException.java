package uk.gov.ida.hub.control.errors;

import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class StateProcessingException extends RuntimeException {
    public static class Mapper implements ExceptionMapper<StateProcessingException> {
        @Override
        public Response toResponse(StateProcessingException exception) {
            var errorId = UUID.randomUUID();
            return Response
                .status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", "STATE_PROCESSING_VALIDATION",
                    "clientMessage", "Invalid transition '" + exception.transitionName + "' for state '" + exception.state.getName() + "'",
                    "audited", false
                ))
                .build();
        }
    }

    private final String transitionName;
    private final VerifySessionState state;

    public StateProcessingException(String transitionName, VerifySessionState state) {
        this.transitionName = transitionName;
        this.state = state;
    }

}