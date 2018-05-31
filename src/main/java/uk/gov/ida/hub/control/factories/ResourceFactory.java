package uk.gov.ida.hub.control.factories;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import uk.gov.ida.hub.control.VerifyControlConfiguration;
import uk.gov.ida.hub.control.resources.SessionResource;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public class ResourceFactory {
    private static final String TRANSLATE_RP_AUTHN_REQUEST = "/translate-rp-authn-request";

    private final VerifyControlConfiguration configuration;
    private final Environment environment;

    public ResourceFactory(VerifyControlConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    public SessionResource createSessionResource() {
        var client = new JerseyClientBuilder(environment).build(SessionResource.class.getSimpleName());
        var samlEngineInvocationBuilder = client
            .target(URI.create(configuration.getSamlEngineUrl()))
            .path(TRANSLATE_RP_AUTHN_REQUEST)
            .request(MediaType.APPLICATION_JSON_TYPE);
        return new SessionResource(configuration.getRedisUrl(), samlEngineInvocationBuilder);
    }
}
