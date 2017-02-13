package uk.gov.register.derivation.core;

import java.util.Set;

public interface RegisterTransformer {

    Set<PartialEntity> transform(Set<PartialEntity> entities);
}
