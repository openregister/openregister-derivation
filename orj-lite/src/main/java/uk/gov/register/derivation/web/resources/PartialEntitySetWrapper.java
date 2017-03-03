package uk.gov.register.derivation.web.resources;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;

import java.util.Collection;
import java.util.Set;

/**
 * Only required so the custom MessageBodyReader has a specific type to return
 */
class PartialEntitySetWrapper {

    final Collection<PartialEntity<Entry>> entitySet;

    PartialEntitySetWrapper(Collection<PartialEntity<Entry>> entitySet) {
        this.entitySet = entitySet;
    }

}
