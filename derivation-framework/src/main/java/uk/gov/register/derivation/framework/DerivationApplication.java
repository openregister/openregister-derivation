package uk.gov.register.derivation.framework;

import io.dropwizard.setup.Environment;
import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.localauthoritybytype.LocalAuthorityByTypeTransformer;
import uk.gov.register.derivation.web.OrjLiteApplication;
import uk.gov.register.derivation.web.OrjLiteConfig;
import uk.gov.register.derivation.web.repo.EntityStore;

public class DerivationApplication extends OrjLiteApplication {

    @Override
    public String getName() {
        return "local-authority-eng-with-derivation";
    }

    @Override
    public void run(final OrjLiteConfig configuration, final Environment environment) {
        super.run(configuration, environment);
        EntityStore<DerivationEntry> derivationStore = new EntityStore<>();
        derivationStores.put("by-type", derivationStore);

        RegisterTransformer registerTransformer = new LocalAuthorityByTypeTransformer();
        registerTransformers.put("by-type", registerTransformer);
    }


    public static void main(String[] args) throws Exception {
        new DerivationApplication().run(args);
    }

}
