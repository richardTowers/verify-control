package uk.gov.ida.hub.control.factories;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import uk.gov.ida.hub.control.VerifyControlConfiguration;
import uk.gov.ida.hub.control.resources.SessionResource;

import java.net.URI;

public class ResourceFactory {
    private final VerifyControlConfiguration configuration;
    private final Environment environment;

    public ResourceFactory(VerifyControlConfiguration configuration, Environment environment) {
        this.configuration = configuration;
        this.environment = environment;
    }

    public SessionResource createSessionResource() throws InterruptedException {
        var client = new JerseyClientBuilder(environment).build(SessionResource.class.getSimpleName());
        var samlEngineTarget = client.target(URI.create(configuration.getSamlEngineUrl()));
        return new SessionResource(configuration.getRedisUrl(), samlEngineTarget);
    }
}
