package uk.gov.ida.hub.control;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.hub.control.resources.SessionResource;

public class VerifyControlApplication extends Application<VerifyControlConfiguration> {

    public static void main(final String[] args) throws Exception {
        new VerifyControlApplication().run(args);
    }

    @Override
    public String getName() {
        return "Verify Control";
    }

    @Override
    public void initialize(final Bootstrap<VerifyControlConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final VerifyControlConfiguration configuration,
                    final Environment environment) {
        var sessionResource = new SessionResource(configuration.getRedis());
        environment.jersey().register(sessionResource);
    }

}
