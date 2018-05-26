package uk.gov.ida.hub.control;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
        // TODO: implement application
    }

}
