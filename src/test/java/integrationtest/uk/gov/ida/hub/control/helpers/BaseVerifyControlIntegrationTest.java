package integrationtest.uk.gov.ida.hub.control.helpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import integrationtest.uk.gov.ida.hub.control.SessionResourceIntegrationTest;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import redis.embedded.RedisCluster;
import uk.gov.ida.hub.control.VerifyControlApplication;
import uk.gov.ida.hub.control.VerifyControlConfiguration;

import javax.ws.rs.client.Client;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class BaseVerifyControlIntegrationTest {
    protected static DropwizardTestSupport<VerifyControlConfiguration> verifyControl;
    protected static RedisCommands<String, String> redisClient;
    protected static Client httpClient;
    private static RedisCluster redisCluster;
    private static WireMockServer wireMockServer;

    @BeforeClass
    public static void beforeClass() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();

        redisCluster = RedisCluster.builder().ephemeral().replicationGroup("master", 1).build();
        redisCluster.start();

        var redisUrl = String.format("redis://localhost:%d", redisCluster.serverPorts().get(0));
        redisClient = RedisClient.create(redisUrl).connect().sync();

        verifyControl = new DropwizardTestSupport<>(
            VerifyControlApplication.class,
            "config.yml",
            ConfigOverride.config("redisUrl", redisUrl),
            ConfigOverride.config("samlEngineUrl", "http://localhost:" + wireMockServer.port())
        );
        verifyControl.before();

        httpClient = new JerseyClientBuilder(verifyControl.getEnvironment())
            .build(SessionResourceIntegrationTest.class.getSimpleName());
    }

    @AfterClass
    public static void afterClass() {
        wireMockServer.stop();
        redisClient.getStatefulConnection().close();
        redisCluster.stop();
        verifyControl.after();
    }

    protected int samlEnginePort() {
        return wireMockServer.port();
    }
}
