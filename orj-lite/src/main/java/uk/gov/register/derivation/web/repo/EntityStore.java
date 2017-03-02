package uk.gov.register.derivation.web.repo;

import uk.gov.register.derivation.core.PartialEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityStore {

    private Map<String, PartialEntity> entitiesByKey;

    public EntityStore() {
        entitiesByKey = new HashMap<>();
    }

    public void store(PartialEntity entity){
        entitiesByKey.put(entity.getKey(), entity);
    }

    public Optional<PartialEntity> findEntity(String key ){
        return Optional.ofNullable(entitiesByKey.get(key));
    }

    public Collection<PartialEntity> allEntities(){
        return entitiesByKey.values();
    }
}
