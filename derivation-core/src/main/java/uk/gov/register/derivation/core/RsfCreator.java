package uk.gov.register.derivation.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RsfCreator {
    private final String TAB = "\t";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public String serializeAsRsf(Set<PartialEntity> entities) {
        return DerivationUtils.toSortedEntrySteam(entities)
                .map(e -> serializeItem(e.entry.getItem()) + "\n" + serializeEntry(e))
                .collect(Collectors.joining("\n"));
    }

    private String serializeEntry(KeyedEntry entryWrapper) {
        return "append-entry" + TAB + entryWrapper.entry.getTimestampAsString() + TAB + entryWrapper.entry.getItemHash() + TAB + entryWrapper.key;
    }

    private String serializeItem(Item item) {
        try {
            return "add-item\t" + objectMapper.writeValueAsString(item.getFields());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }


}
