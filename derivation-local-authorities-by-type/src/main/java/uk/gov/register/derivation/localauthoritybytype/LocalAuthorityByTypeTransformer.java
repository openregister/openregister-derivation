package uk.gov.register.derivation.localauthoritybytype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class LocalAuthorityByTypeTransformer implements RegisterTransformer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LOCAL_AUTHORITIES = "local-authorities";
    private static final String LOCAL_AUTHORITY_TYPE = "local-authority-type";
    private static final String LOCAL_AUTHORITY_ENG = "local-authority-eng";


    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, Function.identity()));
        int currentMaxEntryNumber = currentMaxEntryNumber(state);
        AtomicInteger rollingNumber = new AtomicInteger(0);

        Map<Integer, Entry> entries = newPartialEntities.stream().flatMap(e -> e.getEntries().stream()).collect(Collectors.toMap(e -> e.getEntryNumber(), e -> e));
        Map<String, String> allLocalAuthorities = new HashMap<>();

        entries.values().forEach(entry -> {
            String laType = (String) entry.getItem().getFields().get(LOCAL_AUTHORITY_TYPE);
            String localAuthority = (String) entry.getItem().getFields().get(LOCAL_AUTHORITY_ENG);

            if (allLocalAuthorities.containsKey(localAuthority) && allLocalAuthorities.get(localAuthority).equals(laType)) {
                // No changes that we care about
                return;
            }

            if (allLocalAuthorities.containsKey(localAuthority)) {
                PartialEntity pe = stateMap.get(allLocalAuthorities.get(localAuthority));
                List<String> localAuthList = (List<String>) pe.getRecord().orElseThrow(IllegalStateException::new)
                        .getItem().getFields().get(LOCAL_AUTHORITIES);

                Entry localAuthoritiesEntryWithRemoved = createLocalAuthoritiesEntryRemoving(localAuthority, localAuthList, allLocalAuthorities, pe.getKey(), entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());
                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
            }

            if (!stateMap.containsKey(laType)) {
                // New LA Type, so create the entry in the Map, but also check that local authority is new
                stateMap.put(laType, createLaTypeEntity(laType));
            }

            List<String> currentLocalAuthorities = stateMap.get(laType).getRecord().isPresent()
                    ? (List<String>) stateMap.get(laType).getRecord().get().getItem().getFields().get(LOCAL_AUTHORITIES)
                    : new ArrayList<>();
            Entry newLocalAuthoritiesEntry = createLocalAuthoritiesEntryAdding(localAuthority, currentLocalAuthorities, allLocalAuthorities, laType, entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());
            stateMap.get(laType).getEntries().add(newLocalAuthoritiesEntry);
        });

        return new HashSet<>(stateMap.values());
    }

    private int currentMaxEntryNumber(Set<PartialEntity> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }

    private Entry createLocalAuthoritiesEntryAdding(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.add(localAuthority);
        allLocalAuthorities.put(localAuthority, localAuthorityType);
        return createLocalAuthoritiesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createLocalAuthoritiesEntryRemoving(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.remove(localAuthority);
        allLocalAuthorities.remove(localAuthority);
        return createLocalAuthoritiesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createLocalAuthoritiesEntry(List<String> newLocalAuthorities, String localAuthorityType, int entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(LOCAL_AUTHORITY_TYPE, localAuthorityType);
        newFields.put(LOCAL_AUTHORITIES, newLocalAuthorities);
        Item newLocalAuthoritiesItem = new Item(newFields);
        Entry newLocalAuthoritiesEntry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
        newLocalAuthoritiesEntry.setItem(newLocalAuthoritiesItem);
        return newLocalAuthoritiesEntry;
    }

    private PartialEntity createLaTypeEntity(String laType) {
        PartialEntity laTypeEntity = new PartialEntity(laType);
        Map<String, Object> fields = new HashMap<>();
        fields.put(LOCAL_AUTHORITY_TYPE, laType);
        LinkedList<String> localAuthList = new LinkedList<>();
        fields.put(LOCAL_AUTHORITIES, localAuthList);
        return laTypeEntity;
    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }


}
