package uk.gov.register.derivation.web.serialization;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;

import java.util.Collection;

/**
 * Only required so the custom MessageBodyReader has a specific type to return
 */
public class PartialEntitiesWrapper {

    public final Collection<PartialEntity<Entry>> entities;

    PartialEntitiesWrapper(Collection<PartialEntity<Entry>> entities) {
        this.entities = entities;
    }

}
