package uk.gov.register.derivation.web.resources;

import uk.gov.register.derivation.core.PartialEntity;

import java.util.Set;

/**
 * Only required so the custom MessageBodyReader has a specific type to return
 */
class PartialEntitySetWrapper {

    final Set<PartialEntity> entitySet;

    PartialEntitySetWrapper(Set<PartialEntity> entitySet) {
        this.entitySet = entitySet;
    }

}
