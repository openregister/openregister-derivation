package uk.gov.register.derivation.generic.transformers;

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

public class CountryGroupByFirstLetterTransformer implements Transformer {
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

                Entry localAuthoritiesEntryWithRemoved = createCountriesEntryRemoving(countryName, localAuthList, allItems, pe.getKey(), entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());
                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
            }

            if (!stateMap.containsKey(countryCode)) {
                // New LA Type, so create the entry in the Map, but also check that local authority is new
                stateMap.put(countryCode, createCountryCodeEntity(countryCode));
            }

            List<String> currentLocalAuthorities = stateMap.get(countryCode).getRecord().isPresent()
                    ? (List<String>) stateMap.get(countryCode).getRecord().get().getItem().getFields().get(COUNTRIES)
                    : new ArrayList<>();

            if (currentLocalAuthorities == null) {
                currentLocalAuthorities = new ArrayList<>();
            }

            Entry newLocalAuthoritiesEntry = createCountriesEntryAdding(countryName, currentLocalAuthorities, allItems, countryCode, entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());
            stateMap.get(countryCode).getEntries().add(newLocalAuthoritiesEntry);
        });
    }

    private Entry createCountriesEntryAdding(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.add(localAuthority);
        allLocalAuthorities.put(localAuthority, localAuthorityType);
        return createCountriesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createCountriesEntryRemoving(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.remove(localAuthority);
        allLocalAuthorities.remove(localAuthority);
        return createCountriesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createCountriesEntry(List<String> newLocalAuthorities, String localAuthorityType, int entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(COUNTRY_CODE, localAuthorityType);
        newFields.put(COUNTRIES, newLocalAuthorities);
        Item newLocalAuthoritiesItem = new Item(newFields);
        Entry newLocalAuthoritiesEntry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
        newLocalAuthoritiesEntry.setItem(newLocalAuthoritiesItem);
        return newLocalAuthoritiesEntry;
    }

    private PartialEntity createCountryCodeEntity(String countryCode) {
        PartialEntity countryCodeEntity = new PartialEntity(countryCode);
        Map<String, Object> fields = new HashMap<>();
        fields.put(COUNTRY_CODE, countryCode);
        LinkedList<String> localAuthList = new LinkedList<>();
        fields.put(COUNTRIES, localAuthList);
        return countryCodeEntity;
    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
