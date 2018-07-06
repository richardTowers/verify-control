package uk.gov.ida.hub.control.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.hub.control.statechart.VerifySessionState;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class EntityNotEnabledException extends RuntimeException {
    public static class Mapper implements ExceptionMapper<EntityNotEnabledException> {

        private static final Logger LOG = LoggerFactory.getLogger(Mapper.class.getCanonicalName());

        @Override
        public Response toResponse(EntityNotEnabledException exception) {
            var errorId = UUID.randomUUID();
            String clientMessage = "Entity '" + exception.entityId + "' is not enabled for this session. In state '" + exception.state.getName() + "'";
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

    private final String entityId;
    private final VerifySessionState state;

    public EntityNotEnabledException(String entityId, VerifySessionState state) {
        this.entityId = entityId;
        this.state = state;
    }

}
