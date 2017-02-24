package uk.gov.register.derivation.generic.groupers;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;

import java.util.Collection;
import java.util.Map;

public interface Grouper {
    void group(Collection<Entry> entries, int currentMaxEntryNumber, Map<String, String> allItems, Map<String, PartialEntity> stateMap);
}
