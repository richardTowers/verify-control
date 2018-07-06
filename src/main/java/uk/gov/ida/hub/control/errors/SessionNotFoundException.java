package uk.gov.ida.hub.control.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class SessionNotFoundException extends Exception {
    public static class Mapper implements ExceptionMapper<SessionNotFoundException> {

        private static final Logger LOG = LoggerFactory.getLogger(Mapper.class.getCanonicalName());

        @Override
        public Response toResponse(SessionNotFoundException exception) {
            var errorId = UUID.randomUUID();
            LOG.error("Session not found for session id " + exception.sessionId, exception);
            return Response
                .status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", "SESSION_NOT_FOUND",
                    "clientMessage", "",
                    "audited", false
                ))
                .build();
        }
    }

    private final String sessionId;

    public SessionNotFoundException(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
