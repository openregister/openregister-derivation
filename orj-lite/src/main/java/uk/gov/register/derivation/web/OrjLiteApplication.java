package uk.gov.register.derivation.web;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

import io.dropwizard.setup.Bootstrap;
import uk.gov.register.derivation.web.health.BogusHealthCheck;
import uk.gov.register.derivation.web.repo.EntityStore;
import uk.gov.register.derivation.web.resources.RecordResource;
import uk.gov.register.derivation.web.resources.RecordsResource;
import uk.gov.register.derivation.web.resources.RegisterMessageBodyReader;

import javax.ws.rs.ext.MessageBodyReader;

public class OrjLiteApplication extends Application<OrjLiteConfig> {

        public static void main(final String[] args) throws Exception {
            new OrjLiteApplication().run(args);
        }

        @Override
        public String getName() {
            return "orj-lite";
        }

        @Override
        public void initialize(final Bootstrap<OrjLiteConfig> bootstrap) {
            // TODO: application initialization
        }

        @Override
        public void run(final OrjLiteConfig configuration,
                        final Environment environment) {

            final BogusHealthCheck healthCheck = new BogusHealthCheck();
            environment.healthChecks().register("bogus", healthCheck);

            MessageBodyReader registerMessageBodyReader = new RegisterMessageBodyReader();
            environment.jersey().register(registerMessageBodyReader);

            EntityStore entityStore = new EntityStore();

            RecordsResource recordsResource = new RecordsResource(entityStore);
            environment.jersey().register(recordsResource);

            RecordResource recordResource = new RecordResource(entityStore);
            environment.jersey().register(recordResource);
        }

    }


