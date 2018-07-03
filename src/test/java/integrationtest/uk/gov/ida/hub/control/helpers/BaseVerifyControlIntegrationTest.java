package integrationtest.uk.gov.ida.hub.control.helpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import integrationtest.uk.gov.ida.hub.control.SessionResourceIntegrationTest;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
    private static WireMockServer samlEngineMockServer;
    private static WireMockServer configMockServer;
    private static WireMockServer samlSoapProxyMockServer;

    @BeforeClass
    public static void beforeClass() {
        samlEngineMockServer = new WireMockServer(options().dynamicPort());
        configMockServer = new WireMockClassRule(options().dynamicPort());
        samlSoapProxyMockServer = new WireMockClassRule(options().dynamicPort());
        samlEngineMockServer.start();
        configMockServer.start();
        samlSoapProxyMockServer.start();

        redisCluster = RedisCluster.builder().ephemeral().replicationGroup("master", 1).build();
        redisCluster.start();

        var redisUrl = String.format("redis://localhost:%d", redisCluster.serverPorts().get(0));
        redisClient = RedisClient.create(redisUrl).connect().sync();

        verifyControl = new DropwizardTestSupport<>(
            VerifyControlApplication.class,
            "config.yml",
            ConfigOverride.config("redisUrl", redisUrl),
            ConfigOverride.config("samlEngineUrl", "http://localhost:" + samlEngineMockServer.port()),
            ConfigOverride.config("configUrl", "http://localhost:" + configMockServer.port()),
            ConfigOverride.config("samlSoapProxyUrl", "http://localhost:" + samlSoapProxyMockServer.port())
        );
        verifyControl.before();

        httpClient = new JerseyClientBuilder(verifyControl.getEnvironment())
            .withProperty(ClientProperties.CONNECT_TIMEOUT, 1000)
            .withProperty(ClientProperties.READ_TIMEOUT, 1000)
            .build(SessionResourceIntegrationTest.class.getSimpleName());
    }

    @AfterClass
    public static void afterClass() {
        samlEngineMockServer.stop();
        configMockServer.stop();
        samlSoapProxyMockServer.stop();
        redisClient.getStatefulConnection().close();
        verifyControl.after();
    }

    @Before
    public void before() {
        if (!redisCluster.isActive()) {
            redisCluster.start();
        }
    }

    @After
    public void after() {
        samlEngineMockServer.resetAll();
        configMockServer.resetAll();
        samlSoapProxyMockServer.resetAll();
        redisCluster.stop();
    }

    protected int samlEnginePort() {
        return samlEngineMockServer.port();
    }

    protected int configPort() {
        return configMockServer.port();
    }

    protected int samlSoapProxyPort() {
        return samlSoapProxyMockServer.port();
    }
}
