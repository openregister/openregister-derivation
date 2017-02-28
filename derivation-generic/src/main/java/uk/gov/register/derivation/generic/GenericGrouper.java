package uk.gov.register.derivation.generic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.generic.groupings.Grouping;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericGrouper implements Grouper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void group(Collection<Entry> entries, int currentMaxEntryNumber, Map<String, List<String>> allItems, Map<String, PartialEntity> stateMap, Grouping grouping) {
        AtomicInteger rollingNumber = new AtomicInteger(0);

        entries.forEach(entry -> {
            String groupKey = entry.getItem().getFields().containsKey(grouping.getKeyFieldName())
                    ? entry.getItem().getFields().get(grouping.getKeyFieldName()).toString()
                    : entry.getItem().getFields().get(grouping.getItemFieldName()).toString();

            String groupItem = (String) entry.getItem().getFields().get(grouping.getItemField());

            List<String> groupKeys = grouping.calculateKeys(entry.getItem());

            groupKeys.forEach(subKey -> {
                if (allItems.containsKey(subKey) && allItems.get(subKey).contains(groupKey)) {
                    // No changes that we care about - item has not moved groups
                    return;
                }

                if (allItems.containsKey(subKey) && stateMap.containsKey(groupItem)) {
                    // Item has moved groups
//                    PartialEntity pe = stateMap.get(allItems.get(groupItem));
                    PartialEntity pe = stateMap.get(groupItem);
                    List<String> groupItems = (List<String>) pe.getRecord().orElseThrow(IllegalStateException::new)
                            .getItem().getFields().get(grouping.getItemFieldName());

                    Entry removingEntry = createRemovingEntry(groupKey, subKey, grouping, allItems, groupItems,
                            entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());

                    pe.getEntries().add(removingEntry);
                }

                if (!stateMap.containsKey(groupKey)) {
                    stateMap.put(groupKey, createGroupingEntity(groupKey, grouping));
                }

                List<String> groupItems = stateMap.get(groupKey).getRecord().isPresent()
                        ? (List<String>) stateMap.get(groupKey).getRecord().get().getItem().getFields().get(grouping.getItemFieldName())
                        : new ArrayList<>();

                if (groupItems == null) {
                    groupItems = new ArrayList<>();
                }

                Entry newEntry = createAddingEntry(groupKey, subKey, grouping, allItems, groupItems,
                        entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());

                stateMap.get(groupKey).getEntries().add(newEntry);
            });
        });
    }

    private Entry createAddingEntry(String groupKey, String subKey, Grouping grouping, Map<String, List<String>> allItems, List<String> currentItems, int entryNumber) {
        List<String> updatedGroupValues = new ArrayList<>(currentItems);
        updatedGroupValues.add(groupKey);

        if (!allItems.containsKey(groupKey)) {
            allItems.put(groupKey, new ArrayList<>());
        }
        allItems.get(groupKey).add(subKey);

        return createGroupingEntry(subKey, updatedGroupValues, grouping, entryNumber);
    }

    private Entry createRemovingEntry(String groupKey, String subKey, Grouping grouping, Map<String, List<String>> allItems, List<String> currentItems, int entryNumber) {
        List<String> updatedGroupValues = new ArrayList<>(currentItems);
        updatedGroupValues.remove(groupKey);

        if (allItems.get(subKey).size() > 1) {
            allItems.get(subKey).remove(groupKey);
        }
        else {
            allItems.remove(subKey);
        }

        return createGroupingEntry(groupKey, updatedGroupValues, grouping, entryNumber);
    }

    private Entry createGroupingEntry(String groupKey, List<String> groupItems, Grouping grouping, int entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(grouping.getKeyFieldName(), groupKey);
        newFields.put(grouping.getItemFieldName(), groupItems);
        Item item = new Item(newFields);
        Entry entry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
        entry.setItem(item);
        return entry;
    }

    private PartialEntity createGroupingEntity(String groupKey, Grouping grouping) {
        PartialEntity entity = new PartialEntity(groupKey);
        Map<String, Object> fields = new HashMap<>();
        fields.put(grouping.getKeyFieldName(), groupKey);
        LinkedList<String> groupItems = new LinkedList<>();
        fields.put(grouping.getItemFieldName(), groupItems);
        return entity;
    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
