package integrationtest.uk.gov.ida.hub.control;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.hub.control.VerifyControlApplication;
import uk.gov.ida.hub.control.VerifyControlConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionResourceIntegrationTest {
    private static Client client;

    @ClassRule
    public static DropwizardAppRule<VerifyControlConfiguration> verifyControlAppRule = new DropwizardAppRule<>(
        VerifyControlApplication.class,
        "config.yml"
    );

    @BeforeClass
    public static void beforeClass() {
        client = new JerseyClientBuilder(verifyControlAppRule.getEnvironment()).build(SessionResourceIntegrationTest.class.getSimpleName());
    }

    @Test
    public void shouldCreateSession() {
        var session = createASession();
        assertThat(session.getStatus()).isEqualTo(201);
    }

    private Response createASession() {
        var url = String.format("http://localhost:%s/policy/session", verifyControlAppRule.getLocalPort());
        System.out.println(url);
        return client
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(null);
    }
}
