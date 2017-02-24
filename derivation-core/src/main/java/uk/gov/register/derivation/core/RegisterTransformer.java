package uk.gov.register.derivation.core;

import java.util.List;
import java.util.Set;

public interface RegisterTransformer {
    Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state, List<String> filters, List<String> groupers);
}
