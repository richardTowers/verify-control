package uk.gov.ida.hub.control.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class IdpDisabledException extends Exception {
    public static class Mapper implements ExceptionMapper<IdpDisabledException> {
        @Override
        public Response toResponse(IdpDisabledException exception) {
            var errorId = UUID.randomUUID();
            return Response
                .status(403)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", "IDP_DISABLED",
                    "clientMessage", "IdP disabled: " + exception.issuer,
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
