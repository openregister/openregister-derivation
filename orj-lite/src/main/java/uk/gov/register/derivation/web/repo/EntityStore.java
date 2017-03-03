package uk.gov.register.derivation.web.repo;

import uk.gov.register.derivation.core.AbstractEntry;
import uk.gov.register.derivation.core.PartialEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityStore<T extends AbstractEntry> {

    private Map<String, PartialEntity<T>> entitiesByKey;

    public EntityStore() {
        entitiesByKey = new HashMap<>();
    }

    public void merge(PartialEntity<T> update) {
        String key = update.getKey();
        if (entitiesByKey.containsKey(key)) {
            PartialEntity<T> currentEntity = entitiesByKey.get(key);
            currentEntity.getEntries().addAll(update.getEntries());
        } else {
            entitiesByKey.put(key, update);
        }
    }

    public Optional<PartialEntity<T>> findEntity(String key) {
        return Optional.ofNullable(entitiesByKey.get(key));
    }

    public Collection<PartialEntity<T>> allEntities() {
        return entitiesByKey.values();
    }

}
