package uk.gov.register.derivation.generic.groupers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericGrouper implements Grouper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String COUNTRIES = "countries";
    private static final String COUNTRY_CODE = "name";
    private static final String COUNTRY_NAME = "name";

    @Override
    public void group(Collection<Entry> entries, int currentMaxEntryNumber, Map<String, String> allItems, Map<String, PartialEntity> stateMap) {
        AtomicInteger rollingNumber = new AtomicInteger(0);

        entries.forEach(entry -> {
            String groupKey = Character.toString(entry.getItem().getFields().get(COUNTRY_CODE).toString().charAt(0));
            String groupItem = (String) entry.getItem().getFields().get(COUNTRY_NAME);

            if (allItems.containsKey(groupItem) && allItems.get(groupItem).equals(groupKey)) {
                // No changes that we care about - item has not moved groups
                return;
            }

            if (allItems.containsKey(groupItem)) {
                // Item has moved groups
                PartialEntity pe = stateMap.get(allItems.get(groupItem));
                List<String> groupItems = (List<String>) pe.getRecord().orElseThrow(IllegalStateException::new)
                        .getItem().getFields().get(COUNTRIES);

//                Entry localAuthoritiesEntryWithRemoved = createCountriesEntryRemoving(countryName, localAuthList, allItems, pe.getKey(), entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());

                Entry localAuthoritiesEntryWithRemoved = createRemovingEntry(
                        new AbstractMap.SimpleEntry<>(COUNTRY_CODE, groupKey),
                        new AbstractMap.SimpleEntry<>(COUNTRIES, groupItem),
                        allItems,
                        groupItems,
                        entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());

                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
            }

            if (!stateMap.containsKey(groupKey)) {
                // New LA Type, so create the entry in the Map, but also check that local authority is new
                //stateMap.put(countryCode, createCountryCodeEntity(countryCode));

                stateMap.put(
                        groupKey,
                        createGroupingEntity(new AbstractMap.SimpleEntry<>(COUNTRY_CODE, groupKey), groupKey));
            }

            List<String> groupItems = stateMap.get(groupKey).getRecord().isPresent()
                    ? (List<String>) stateMap.get(groupKey).getRecord().get().getItem().getFields().get(COUNTRIES)
                    : new ArrayList<>();

            if (groupItems == null) {
                groupItems = new ArrayList<>();
            }

//            Entry newLocalAuthoritiesEntry = createCountriesEntryAdding(countryName, currentLocalAuthorities, allItems, countryCode, entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());

            Entry newEntry = createAddingEntry(
                    new AbstractMap.SimpleEntry<>(COUNTRY_CODE, groupKey),
                    new AbstractMap.SimpleEntry<>(COUNTRIES, groupItem),
                    allItems,
                    groupItems,
                    entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());

            stateMap.get(groupKey).getEntries().add(newEntry);
        });
    }

    private Entry createAddingEntry(Map.Entry<String, String> groupKey, Map.Entry<String, String> item, Map<String, String> allItems, List<String> currentItems, int entryNumber) {
        List<String> updatedGroupValues = new ArrayList<>(currentItems);
        updatedGroupValues.add(item.getValue());
        allItems.put(item.getValue(), groupKey.getValue());

        return createGroupingEntry(
                new AbstractMap.SimpleEntry(groupKey.getKey(), groupKey.getValue()),
                new AbstractMap.SimpleEntry<>(item.getKey(), updatedGroupValues),
                entryNumber);
    }

    private Entry createRemovingEntry(Map.Entry<String, String> groupKey, Map.Entry<String, String> item, Map<String, String> allItems, List<String> currentItems, int entryNumber) {
        List<String> updatedGroupValues = new ArrayList<>(currentItems);
        updatedGroupValues.remove(groupKey.getValue());
        allItems.remove(groupKey.getValue());

        return createGroupingEntry(
                new AbstractMap.SimpleEntry<>(groupKey.getKey(), groupKey.getValue()),
                new AbstractMap.SimpleEntry<>(item.getKey(), updatedGroupValues),
                entryNumber);
    }

    private Entry createGroupingEntry(Map.Entry<String, Object> groupKey, Map.Entry<String, List<String>> groupItems, int entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(groupKey.getKey(), groupKey.getValue());
        newFields.put(groupItems.getKey(), groupItems.getValue());
        Item item = new Item(newFields);
        Entry entry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
        entry.setItem(item);
        return entry;
    }

    private PartialEntity createGroupingEntity(Map.Entry<String, String> groupKey, String groupItemKey) {
        PartialEntity entity = new PartialEntity(groupKey.getValue());
        Map<String, Object> fields = new HashMap<>();
        fields.put(groupKey.getKey(), groupKey.getValue());
        LinkedList<String> groupItems = new LinkedList<>();
        fields.put(groupItemKey, groupItems);
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
