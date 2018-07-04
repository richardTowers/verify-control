package uk.gov.ida.hub.control.factories;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import uk.gov.ida.hub.control.VerifyControlConfiguration;
import uk.gov.ida.hub.control.clients.ConfigServiceClient;
import uk.gov.ida.hub.control.clients.SamlEngineClient;
import uk.gov.ida.hub.control.clients.SamlSoapProxyClient;
import uk.gov.ida.hub.control.resources.AuthnRequestFromTransactionResource;
import uk.gov.ida.hub.control.resources.Cycle3DataResource;
import uk.gov.ida.hub.control.resources.MatchingServiceResponseResource;
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

    public SessionResource createSessionResource() {
        var client = new JerseyClientBuilder(environment).build(SessionResource.class.getSimpleName());
        var samlEngineTarget = client.target(URI.create(configuration.getSamlEngineUrl()));
        var configServiceTarget = client.target(URI.create(configuration.getConfigUrl()));
        var samlSoapProxyWebTarget = client.target(URI.create(configuration.getSamlSoapProxyUrl()));
        return new SessionResource(
            redisClient,
            new SamlEngineClient(samlEngineTarget),
            new ConfigServiceClient(configServiceTarget),
            new SamlSoapProxyClient(samlSoapProxyWebTarget));
    }

    public AuthnRequestFromTransactionResource createAuthnRequestFromTransactionResource() {
        var client = new JerseyClientBuilder(environment).build(AuthnRequestFromTransactionResource.class.getSimpleName());
        var configServiceTarget = client.target(URI.create(configuration.getConfigUrl()));
        return new AuthnRequestFromTransactionResource(
            redisClient,
            new ConfigServiceClient(configServiceTarget)
        );
    }

    public MatchingServiceResponseResource createMatchingServiceResponseResource() {
        return new MatchingServiceResponseResource(redisClient);
    }

    public Cycle3DataResource createCycle3DataResource() {
        var client = new JerseyClientBuilder(environment).build(Cycle3DataResource.class.getSimpleName());
        var samlSoapProxyWebTarget = client.target(URI.create(configuration.getSamlSoapProxyUrl()));
        return new Cycle3DataResource(redisClient, new SamlSoapProxyClient(samlSoapProxyWebTarget));
    }
}
