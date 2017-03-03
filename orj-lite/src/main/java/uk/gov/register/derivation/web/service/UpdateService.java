package uk.gov.register.derivation.web.service;

import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.web.repo.EntityStore;

import java.util.Collection;

public class UpdateService {

    private final EntityStore<Entry> registerStore;
    private final EntityStore<DerivationEntry> derivationStore;
    private final RegisterTransformer registerTransformer;

    public UpdateService(EntityStore registerStore, EntityStore derivationStore, RegisterTransformer registerTransformer) {
        this.registerStore = registerStore;
        this.derivationStore = derivationStore;
        this.registerTransformer = registerTransformer;
    }

    public void update(Collection<PartialEntity<Entry>> entities){
        entities.forEach(entity -> registerStore.merge(entity));
        Collection<PartialEntity<DerivationEntry>> derivationState = derivationStore.allEntities();
        Collection<PartialEntity<DerivationEntry>> derivationUpdate = registerTransformer.transform(entities, derivationState);
        derivationState.clear();
        derivationUpdate.forEach( entity -> derivationStore.merge(entity));
    }
}
