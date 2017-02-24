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

public class CountryGroupByFirstLetterGrouper implements Grouper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COUNTRIES = "countries";
    private static final String COUNTRY_CODE = "name";
    private static final String COUNTRY_NAME = "name";

    @Override
    public void group(Collection<Entry> entries, int currentMaxEntryNumber, Map<String, String> allItems, Map<String, PartialEntity> stateMap) {
        AtomicInteger rollingNumber = new AtomicInteger(0);

        entries.forEach(entry -> {
            String countryCode = Character.toString(entry.getItem().getFields().get(COUNTRY_CODE).toString().charAt(0));
            String countryName = (String) entry.getItem().getFields().get(COUNTRY_NAME);

            if (allItems.containsKey(countryName) && allItems.get(countryName).equals(countryCode)) {
                // No changes that we care about
                return;
            }

            if (allItems.containsKey(countryName)) {
                PartialEntity pe = stateMap.get(allItems.get(countryName));
                List<String> localAuthList = (List<String>) pe.getRecord().orElseThrow(IllegalStateException::new)
                        .getItem().getFields().get(COUNTRIES);

//                Entry localAuthoritiesEntryWithRemoved = createCountriesEntryRemoving(countryName, localAuthList, allItems, pe.getKey(), entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());

                Entry localAuthoritiesEntryWithRemoved = createRemovingEntry(
                        new AbstractMap.SimpleEntry<>(COUNTRY_CODE, countryCode),
                        new AbstractMap.SimpleEntry<>(COUNTRIES, countryName),
                        allItems,
                        localAuthList,
                        entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());

                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
            }

            if (!stateMap.containsKey(countryCode)) {
                // New LA Type, so create the entry in the Map, but also check that local authority is new
                //stateMap.put(countryCode, createCountryCodeEntity(countryCode));

                stateMap.put(
                        countryCode,
                        createGroupingEntity(new AbstractMap.SimpleEntry<>(COUNTRY_CODE, countryCode), countryCode));
            }

            List<String> currentLocalAuthorities = stateMap.get(countryCode).getRecord().isPresent()
                    ? (List<String>) stateMap.get(countryCode).getRecord().get().getItem().getFields().get(COUNTRIES)
                    : new ArrayList<>();

            if (currentLocalAuthorities == null) {
                currentLocalAuthorities = new ArrayList<>();
            }

//            Entry newLocalAuthoritiesEntry = createCountriesEntryAdding(countryName, currentLocalAuthorities, allItems, countryCode, entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());

            Entry newLocalAuthoritiesEntry = createAddingEntry(
                    new AbstractMap.SimpleEntry<>(COUNTRY_CODE, countryCode),
                    new AbstractMap.SimpleEntry<>(COUNTRIES, countryName),
                    allItems,
                    currentLocalAuthorities,
                    entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());

            stateMap.get(countryCode).getEntries().add(newLocalAuthoritiesEntry);
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

//    private Entry createCountriesEntryAdding(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
//        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
//        updatedLocalAuthorities.add(localAuthority);
//        allLocalAuthorities.put(localAuthority, localAuthorityType);
//        return createCountriesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
//    }
//
//    private Entry createCountriesEntryRemoving(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
//        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
//        updatedLocalAuthorities.remove(localAuthority);
//        allLocalAuthorities.remove(localAuthority);
//        return createCountriesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
//    }

//    private Entry createCountriesEntry(List<String> newLocalAuthorities, String localAuthorityType, int entryNumber) {
//        Map<String, Object> newFields = new HashMap<>();
//        newFields.put(COUNTRY_CODE, localAuthorityType);
//        newFields.put(COUNTRIES, newLocalAuthorities);
//        Item newLocalAuthoritiesItem = new Item(newFields);
//        Entry newLocalAuthoritiesEntry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
//        newLocalAuthoritiesEntry.setItem(newLocalAuthoritiesItem);
//        return newLocalAuthoritiesEntry;
//    }

//    private PartialEntity createCountryCodeEntity(String countryCode) {
//        PartialEntity countryCodeEntity = new PartialEntity(countryCode);
//        Map<String, Object> fields = new HashMap<>();
//        fields.put(COUNTRY_CODE, countryCode);
//        LinkedList<String> localAuthList = new LinkedList<>();
//        fields.put(COUNTRIES, localAuthList);
//        return countryCodeEntity;
//    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
