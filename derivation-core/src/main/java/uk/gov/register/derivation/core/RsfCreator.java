package uk.gov.register.derivation.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UncheckedIOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RsfCreator {
    private final String TAB = "\t";
    private static ObjectMapper objectMapper = new ObjectMapper();

    public String serializeAsRsf(Set<PartialEntity> entities) {
        return entities.stream().flatMap(pe -> getWrappedEntryStream(pe))
                .sorted((e1, e2) -> Integer.compare(e1.entry.getEntryNumber(), e2.entry.getEntryNumber()))
                .map(e -> serializeItem(e.entry.getItem()) + "\n" + serializeEntry(e))
                .collect(Collectors.joining("\n"));
    }

    private Stream<EntryWrapper> getWrappedEntryStream(PartialEntity entity) {
        return entity.getEntries().stream().map(e -> new EntryWrapper(e, entity.getKey()));
    }

    private String serializeEntry(EntryWrapper entryWrapper) {
        return "append-entry" + TAB + entryWrapper.entry.getEntryNumber() + TAB + entryWrapper.entry.getTimestampAsString() + TAB + entryWrapper.entry.getItemHash() + TAB + entryWrapper.key;
    }

    private String serializeItem(Item item) {
        try {
            return "add-item\t" + objectMapper.writeValueAsString(item.getFields());
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private class EntryWrapper {
        private final Entry entry;
        private final String key;

        public EntryWrapper(Entry entry, String key) {
            this.entry = entry;
            this.key = key;
        }
    }
}
