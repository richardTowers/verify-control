package uk.gov.ida.hub.control.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class ConditionNotMetException extends Exception {
    public static class Mapper implements ExceptionMapper<ConditionNotMetException> {

        private static final Logger LOG = LoggerFactory.getLogger(Mapper.class.getCanonicalName());

        @Override
        public Response toResponse(ConditionNotMetException exception) {
            var errorId = UUID.randomUUID();
            String clientMessage = "Condition not met: " + exception.getMessage();
            LOG.error(clientMessage, exception);
            return Response
                .status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", "STATE_PROCESSING_VALIDATION",
                    "clientMessage", clientMessage,
                    "audited", false
                ))
                .build();
        }
    }

    public ConditionNotMetException(String message) {
        super(message);
    }
}
