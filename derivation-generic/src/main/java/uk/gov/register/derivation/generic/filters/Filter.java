package uk.gov.register.derivation.generic.filters;

import uk.gov.register.derivation.core.PartialEntity;

import java.util.Map;

public interface Filter {
    void apply(PartialEntity entity, Map<String, PartialEntity> stateMap);
}
