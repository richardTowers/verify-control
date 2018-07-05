package uk.gov.ida.hub.control;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.hub.control.errors.ApiBadRequestException;
import uk.gov.ida.hub.control.errors.ConditionNotMetException;
import uk.gov.ida.hub.control.errors.EntityNotEnabledException;
import uk.gov.ida.hub.control.errors.IdpDisabledException;
import uk.gov.ida.hub.control.errors.SessionNotFoundException;
import uk.gov.ida.hub.control.errors.StateProcessingException;
import uk.gov.ida.hub.control.factories.ResourceFactory;
import uk.gov.ida.hub.control.resources.ResponseFromIdpResource;

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
                    final Environment environment) throws InterruptedException {
        var resourceFactory = new ResourceFactory(configuration, environment);
        var jersey = environment.jersey();

        // Resources
        jersey.register(resourceFactory.createSessionResource());
        jersey.register(resourceFactory.createAuthnRequestFromTransactionResource());
        jersey.register(resourceFactory.createMatchingServiceResponseResource());
        jersey.register(resourceFactory.createCycle3DataResource());
        jersey.register(resourceFactory.createResponseFromIdpResource());

        // Exception Mappers
        jersey.register(new SessionNotFoundException.Mapper());
        jersey.register(new StateProcessingException.Mapper());
        jersey.register(new EntityNotEnabledException.Mapper());
        jersey.register(new ApiBadRequestException.Mapper());
        jersey.register(new ConditionNotMetException.Mapper());
        jersey.register(new IdpDisabledException.Mapper());
    }

}
