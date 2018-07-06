package uk.gov.ida.hub.control.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class IdpDisabledException extends Exception {
    public static class Mapper implements ExceptionMapper<IdpDisabledException> {

        private static final Logger LOG = LoggerFactory.getLogger(Mapper.class.getCanonicalName());

        @Override
        public Response toResponse(IdpDisabledException exception) {
            var errorId = UUID.randomUUID();
            String clientMessage = "IdP disabled: " + exception.issuer;
            LOG.error(clientMessage, exception);
            return Response
                .status(403)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", "IDP_DISABLED",
                    "clientMessage", clientMessage,
                    "audited", false
                ))
                .build();
        }
    }
    private final String issuer;
    public IdpDisabledException(String issuer) {
        this.issuer = issuer;
    }
}
