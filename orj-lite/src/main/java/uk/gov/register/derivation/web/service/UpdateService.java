package uk.gov.register.derivation.web.service;

import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.web.repo.EntityStore;

import java.util.Collection;
import java.util.Map;

public class UpdateService {

    private final EntityStore<Entry> registerStore;
    private final Map<String, EntityStore<DerivationEntry>> derivationStores;
    private final Map<String, RegisterTransformer> registerTransformers;

    public UpdateService(EntityStore registerStore, Map<String, EntityStore<DerivationEntry>> derivationStores, Map<String, RegisterTransformer> registerTransformers) {
        this.registerStore = registerStore;
        this.derivationStores = derivationStores;
        this.registerTransformers = registerTransformers;
    }

    public void update(Collection<PartialEntity<Entry>> entities) {
        entities.forEach(entity -> registerStore.merge(entity));
        derivationStores.keySet().forEach(derivationName -> {
                    RegisterTransformer registerTransformer = registerTransformers.get(derivationName);
                    EntityStore<DerivationEntry> derivationStore = derivationStores.get(derivationName);
                    Collection<PartialEntity<DerivationEntry>> derivationState = derivationStore.allEntities();
                    Collection<PartialEntity<DerivationEntry>> derivationUpdate = registerTransformer.transform(entities, derivationState);
                    derivationStore.clear();
                    derivationUpdate.forEach(entity -> derivationStore.merge(entity));
                }
        );

    }
}
