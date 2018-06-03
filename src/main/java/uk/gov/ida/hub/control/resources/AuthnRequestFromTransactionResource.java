package uk.gov.ida.hub.control.resources;

import com.codahale.metrics.annotation.Timed;
import uk.gov.ida.hub.control.dtos.SelectIdpDto;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/policy/received-authn-request")
public class AuthnRequestFromTransactionResource {

    @POST
    @Path("/{sessionId}/select-identity-provider")
    @Timed
    public Response selectIdentityProvider(@PathParam("sessionId") String sessionId, @Valid SelectIdpDto selectIdpDto) {
        return Response.status(201).build();
    }
}
