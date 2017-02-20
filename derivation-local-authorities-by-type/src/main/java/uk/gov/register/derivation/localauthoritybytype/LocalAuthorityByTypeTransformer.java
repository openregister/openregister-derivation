package uk.gov.register.derivation.localauthoritybytype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

import static java.util.stream.Collectors.toMap;

public class LocalAuthorityByTypeTransformer implements RegisterTransformer {

    private static final String LOCAL_AUTHORITIES = "local-authorities";
    private static final String LOCAL_AUTHORITY_TYPE = "local-authority-type";
    private static final String LOCAL_AUTHORITY_ENG = "local-authority-eng";
    private final ObjectMapper objectMapper ;

    public LocalAuthorityByTypeTransformer() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, Function.identity()));
        AtomicInteger entryNumber = new AtomicInteger(currentMaxEntryNumber(state));
        newPartialEntities.forEach(newEntity -> newEntity.getEntries().forEach(entry -> {
            String laType = (String) entry.getItem().getFields().get(LOCAL_AUTHORITY_TYPE);
            String localAuthority = (String) entry.getItem().getFields().get(LOCAL_AUTHORITY_ENG);

            if (stateMap.containsKey(laType)) {
                List<String> currentLocalAuthorities = (List<String>) stateMap.get(laType).getRecord()
                        .orElseThrow(IllegalStateException::new).getItem().getFields().get(LOCAL_AUTHORITIES);

                if (!currentLocalAuthorities.contains(localAuthority)) {
                    findEntityByAuthority(localAuthority, state)
                            .ifPresent(pe -> {
                                List<String> localAuthList = (List<String>) pe.getRecord()
                                        .orElseThrow(IllegalStateException::new).getItem().getFields().get(LOCAL_AUTHORITIES);

                                Entry localAuthoritiesEntryWithRemoved = createLocalAuthoritiesEntryRemoving(localAuthority, localAuthList, pe.getKey(), entryNumber);
                                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
                            });
                    Entry newLocalAuthoritiesEntry = createLocalAuthoritiesEntryAdding(localAuthority, currentLocalAuthorities, laType, entryNumber);
                    stateMap.get(laType).getEntries().add(newLocalAuthoritiesEntry);

                }
            } else {
                stateMap.put(laType, createLaTypeEntity(laType, localAuthority, entryNumber));
            }
        }));
        return new HashSet<>(stateMap.values());
    }

    private Optional<PartialEntity> findEntityByAuthority(String localAuthority, Set<PartialEntity> state) {
        return state.stream().filter(e -> ((List<String>) e.getRecord().orElseThrow(IllegalStateException::new)
                .getItem().getFields().get(LOCAL_AUTHORITIES)).contains(localAuthority)).findFirst();
    }

    private int currentMaxEntryNumber(Set<PartialEntity> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }

    private Entry createLocalAuthoritiesEntryAdding(String localAuthority, List<String> currentLocalAuthorities, String localAuthorityType, AtomicInteger entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.add(localAuthority);
        return createLocalAuthoritiesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createLocalAuthoritiesEntryRemoving(String localAuthority, List<String> currentLocalAuthorities, String localAuthorityType, AtomicInteger entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.remove(localAuthority);
        return createLocalAuthoritiesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createLocalAuthoritiesEntry(List<String> newLocalAuthorities, String localAuthorityType, AtomicInteger entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(LOCAL_AUTHORITY_TYPE, localAuthorityType);
        newFields.put(LOCAL_AUTHORITIES, newLocalAuthorities);
        Item newLocalAuthoritiesItem = new Item(newFields);
        Entry newLocalAuthoritiesEntry = new Entry(entryNumber.getAndIncrement(), Instant.now(), hashValue(newFields));
        newLocalAuthoritiesEntry.setItem(newLocalAuthoritiesItem);
        return newLocalAuthoritiesEntry;
    }

    private PartialEntity createLaTypeEntity(String laType, String localAuth, AtomicInteger entryNumber) {
        PartialEntity laTypeEntity = new PartialEntity(laType);
        Map<String, Object> fields = new HashMap<>();
        fields.put(LOCAL_AUTHORITY_TYPE, laType);
        LinkedList<String> localAuthList = new LinkedList<>();
        localAuthList.add(localAuth);
        fields.put(LOCAL_AUTHORITIES, localAuthList);
        Item item = new Item(fields);
        Entry entry = new Entry(entryNumber.getAndIncrement(), Instant.now(), hashValue(fields));
        entry.setItem(item);
        laTypeEntity.getEntries().add(entry);
        return laTypeEntity;
    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return "sha-256:" + DigestUtils.sha256Hex(objectMapper.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }


}
