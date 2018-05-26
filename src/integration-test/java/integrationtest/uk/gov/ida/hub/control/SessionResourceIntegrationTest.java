package integrationtest.uk.gov.ida.hub.control;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.embedded.RedisCluster;
import uk.gov.ida.hub.control.VerifyControlApplication;
import uk.gov.ida.hub.control.VerifyControlConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionResourceIntegrationTest {

    private static RedisCluster redisCluster;
    private static DropwizardTestSupport<VerifyControlConfiguration> verifyControl;
    private static RedisCommands<String, String> redisClient;
    private static Client httpClient;

    @BeforeClass
    public static void beforeClass() {
        redisCluster = RedisCluster.builder().ephemeral().replicationGroup("master", 1).build();
        redisCluster.start();

        var redisUrl = String.format("redis://localhost:%d", redisCluster.serverPorts().get(0));
        redisClient = RedisClient.create(redisUrl).connect().sync();

        verifyControl = new DropwizardTestSupport<>(
            VerifyControlApplication.class,
            "config.yml",
            ConfigOverride.config("redisUrl", redisUrl)
        );
        verifyControl.before();

        httpClient = new JerseyClientBuilder(verifyControl.getEnvironment())
            .build(SessionResourceIntegrationTest.class.getSimpleName());
    }

    @AfterClass
    public static void afterClass() {
        redisCluster.stop();
        verifyControl.after();
    }

    @Test
    public void shouldCreateSession() {
        var response = createASession();
        var sessionId = response.readEntity(String.class);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(redisClient.get("session:" + sessionId)).isEqualTo("banana");
    }

    private Response createASession() {
        var url = String.format("http://localhost:%s/policy/session", verifyControl.getLocalPort());
        return httpClient
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(null);
    }
}
