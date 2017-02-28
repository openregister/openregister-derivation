package uk.gov.register.derivation.localauthoritybytype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.*;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

public abstract class GroupingTransformer extends SwitchingTransformer {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, Function.identity()));

        AtomicInteger entryNumber = new AtomicInteger(currentMaxEntryNumber(state));

        Map<String, String> sourceKeyToGroupingKeyMap = new HashMap<>();
        state.forEach(pe -> {
            Entry record = pe.getRecord().get();
            List<String> localAuthorities = (List<String>) record.getItem().getFields().get(keyListField());
            localAuthorities.forEach(la -> sourceKeyToGroupingKeyMap.put(la, pe.getKey()));
        });

        DerivationUtils.asEntryLog(newPartialEntities).forEach(keyedEntry -> {

            // e.g. local-authority-type
            String groupingFieldValue = (String) keyedEntry.entry.getItem().getFields().get(groupingField());
            // e.g. local-authority-eng
            String sourceKey = keyedEntry.key;

            if (!(sourceKeyToGroupingKeyMap.containsKey(sourceKey) &&
                    sourceKeyToGroupingKeyMap.get(sourceKey).equals(groupingFieldValue))) {

                if (sourceKeyToGroupingKeyMap.containsKey(sourceKey)) {
                    PartialEntity pe = stateMap.get(sourceKeyToGroupingKeyMap.get(sourceKey));
                    int nextEntryNumber = entryNumber.incrementAndGet();
                    Entry removingEntry = getRemovingEntry(sourceKey, pe, nextEntryNumber);
                    pe.getEntries().add(removingEntry);
                    sourceKeyToGroupingKeyMap.remove(sourceKey);
                }
                if (stateMap.containsKey(groupingFieldValue)) {
                    int nextEntryNumber = entryNumber.incrementAndGet();
                    Entry addingEntry = getAddingEntry(stateMap, groupingFieldValue, sourceKey, nextEntryNumber);
                    sourceKeyToGroupingKeyMap.put(sourceKey, groupingFieldValue);
                    stateMap.get(groupingFieldValue).getEntries().add(addingEntry);
                } else {
                    PartialEntity pe = createDerivationEntity(groupingFieldValue);
                    Entry addingEntry = createEntryAdding(sourceKey, emptyList(), groupingFieldValue, entryNumber.incrementAndGet());
                    pe.getEntries().add(addingEntry);
                    stateMap.put(groupingFieldValue, pe);
                }
                sourceKeyToGroupingKeyMap.put(sourceKey, groupingFieldValue);
            }
        });

        return new HashSet<>(stateMap.values());
    }

    private Entry getAddingEntry(Map<String, PartialEntity> stateMap, String groupingFieldValue, String sourceKey, int nextEntryNumber) {
        List<String> currentListForAddition = getKeyList(stateMap.get(groupingFieldValue));
        return createEntryAdding(sourceKey, currentListForAddition, groupingFieldValue, nextEntryNumber);
    }

    private Entry getRemovingEntry(String sourceKey, PartialEntity pe, int nextEntryNumber) {
        List<String> currentListForRemoval = getKeyList(pe);
        return createEntryRemoving(sourceKey, currentListForRemoval, pe.getKey(), nextEntryNumber);
    }

    private List<String> getKeyList(PartialEntity pe) {
        return (List<String>) pe.getRecord().orElseThrow(IllegalStateException::new)
                .getItem().getFields().get(keyListField());
    }

    abstract String groupingField();

    abstract String keyListField();

    private Entry createEntryAdding(String sourceKeyFieldValue, List<String> currentList, String groupingFieldValue, int entryNumber) {
        List<String> updatedList = new ArrayList<>(currentList);
        updatedList.add(sourceKeyFieldValue);
        return createEntry(updatedList, groupingFieldValue, entryNumber);
    }

    private Entry createEntryRemoving(String sourceKeyFieldValue, List<String> currentList, String groupingFieldValue, int entryNumber) {
        List<String> updatedList = new ArrayList<>(currentList);
        updatedList.remove(sourceKeyFieldValue);
        return createEntry(updatedList, groupingFieldValue, entryNumber);
    }

    private Entry createEntry(List<String> newList, String groupingFieldValue, int entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(groupingField(), groupingFieldValue);
        newFields.put(keyListField(), newList);
        Item newItem = new Item(newFields);
        Entry newEntry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
        newEntry.setItem(newItem);
        return newEntry;
    }

    private PartialEntity createDerivationEntity(String groupingFieldValue) {
        PartialEntity laTypeEntity = new PartialEntity(groupingFieldValue);
        Map<String, Object> fields = new HashMap<>();
        fields.put(groupingField(), groupingFieldValue);
        LinkedList<String> keyList = new LinkedList<>();
        fields.put(keyListField(), keyList);
        return laTypeEntity;
    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int currentMaxEntryNumber(Set<PartialEntity> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }


}
