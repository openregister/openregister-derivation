package uk.gov.register.derivation.core;

import java.util.Collection;

public interface RegisterTransformer {
    Collection<PartialEntity<DerivationEntry>> transform(Collection<PartialEntity<Entry>> newPartialEntities,
                                                         Collection<PartialEntity<DerivationEntry>> state);
}
