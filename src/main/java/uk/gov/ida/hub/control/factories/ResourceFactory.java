package uk.gov.ida.hub.control.factories;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.VerifyControlConfiguration;
import uk.gov.ida.hub.control.resources.AuthnRequestFromTransactionResource;
import uk.gov.ida.hub.control.resources.SessionResource;

import java.net.URI;

import static uk.gov.ida.hub.control.helpers.ExponentiallyBackingOffRedisConnector.connectToRedis;

public class ResourceFactory {
    private final VerifyControlConfiguration configuration;
    private final Environment environment;
    private final RedisCommands<String, String> redisClient;

    public ResourceFactory(VerifyControlConfiguration configuration, Environment environment) throws InterruptedException {
        this.configuration = configuration;
        this.environment = environment;
        this.redisClient = connectToRedis(RedisClient.create(configuration.getRedisUrl()));
    }

    public SessionResource createSessionResource() throws InterruptedException {
        var client = new JerseyClientBuilder(environment).build(SessionResource.class.getSimpleName());
        var samlEngineTarget = client.target(URI.create(configuration.getSamlEngineUrl()));
        return new SessionResource(redisClient, samlEngineTarget);
    }

    public AuthnRequestFromTransactionResource createAuthnRequestFromTransactionResource() {
        return new AuthnRequestFromTransactionResource(redisClient);
    }
}
