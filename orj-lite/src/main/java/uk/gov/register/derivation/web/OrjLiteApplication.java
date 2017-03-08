package uk.gov.register.derivation.web;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.web.health.BogusHealthCheck;
import uk.gov.register.derivation.web.repo.EntityStore;
import uk.gov.register.derivation.web.resources.DerivationResource;
import uk.gov.register.derivation.web.resources.RecordResource;
import uk.gov.register.derivation.web.resources.RecordsResource;
import uk.gov.register.derivation.web.serialization.RegisterMessageBodyReader;
import uk.gov.register.derivation.web.service.UpdateService;

import javax.ws.rs.ext.MessageBodyReader;
import java.util.HashMap;
import java.util.Map;

public class OrjLiteApplication extends Application<OrjLiteConfig> {

    protected Map<String, EntityStore<DerivationEntry>> derivationStores ;

    protected Map<String, RegisterTransformer> registerTransformers;

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

        EntityStore<Entry> registerStore = new EntityStore<>();

        derivationStores = new HashMap<>();
        registerTransformers = new HashMap<>();

        UpdateService updateService = new UpdateService(registerStore, derivationStores, registerTransformers);

        RecordsResource recordsResource = new RecordsResource(registerStore, updateService);
        environment.jersey().register(recordsResource);

        RecordResource recordResource = new RecordResource(registerStore);
        environment.jersey().register(recordResource);

        DerivationResource derivationResource = new DerivationResource(derivationStores);
        environment.jersey().register(derivationResource);
    }

}


