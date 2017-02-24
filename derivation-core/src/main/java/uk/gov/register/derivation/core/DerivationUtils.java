package uk.gov.register.derivation.core;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class DerivationUtils {

    public static Stream<KeyedEntry> toSortedEntrySteam(Set<PartialEntity> entities) {
        return entities.stream().flatMap(pe -> getKeyedEntryStream(pe))
                .sorted(Comparator.comparingInt(e2 -> e2.entry.getEntryNumber()));
    }

    private static Stream<KeyedEntry> getKeyedEntryStream(PartialEntity entity) {
        return entity.getEntries().stream().map(e -> new KeyedEntry(e, entity.getKey()));
    }
}
