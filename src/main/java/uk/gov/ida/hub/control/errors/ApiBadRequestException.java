package uk.gov.ida.hub.control.errors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

import static uk.gov.ida.hub.control.helpers.Aliases.mapOf;

public class ApiBadRequestException extends Exception {
    public static class Mapper implements ExceptionMapper<ApiBadRequestException> {
        @Override
        public Response toResponse(ApiBadRequestException exception) {
            var errorId = UUID.randomUUID();
            return Response
                .status(400)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapOf(
                    "errorId", errorId,
                    "exceptionType", exception.exceptionType,
                    "clientMessage", "Bad request for call to " + exception.url + ". Message was '" + exception.clientMessage + "'.",
                    "audited", false
                ))
                .build();
        }
    }
    private final String url;
    private final String clientMessage;
    private final String exceptionType;

    public ApiBadRequestException(String url, String clientMessage, String exceptionType) {
        this.url = url;
        this.clientMessage = clientMessage;
        this.exceptionType = exceptionType;
    }
}
