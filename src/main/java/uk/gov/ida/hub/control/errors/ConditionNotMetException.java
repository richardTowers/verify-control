package uk.gov.ida.hub.control.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class ConditionNotMetException extends Exception {
    public static class Mapper implements ExceptionMapper<ConditionNotMetException> {
        @Override
        public Response toResponse(ConditionNotMetException exception) {
            var errorId = UUID.randomUUID();
            return Response
                .status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", "STATE_PROCESSING_VALIDATION",
                    "clientMessage", "Condition not met: " + exception.getMessage(),
                    "audited", false
                ))
                .build();
        }
    }

    public ConditionNotMetException(String message) {
        super(message);
    }
}
