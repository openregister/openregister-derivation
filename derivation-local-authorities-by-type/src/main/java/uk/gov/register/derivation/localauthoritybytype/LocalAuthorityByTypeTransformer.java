package uk.gov.register.derivation.localauthoritybytype;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gov.register.derivation.core.*;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class LocalAuthorityByTypeTransformer implements RegisterTransformer {

    private static final String LOCAL_AUTHORITY_TYPE = "local-authority-type";
    public static final String LOCAL_AUTHORITY_ENG = "local-authority-eng";
    private final ObjectMapper objectMapper;

    public LocalAuthorityByTypeTransformer() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Override
    public Collection<PartialEntity<DerivationEntry>> transform(Collection<PartialEntity<Entry>> updateEntities,
                                                                Collection<PartialEntity<DerivationEntry>> state) {

        final Map<String, PartialEntity<DerivationEntry>> stateMap = state.stream().collect(toMap(PartialEntity::getKey, identity()));

        AtomicInteger entryNumber = new AtomicInteger(currentMaxEntryNumber(state));

        updateEntities.forEach(newEntity -> newEntity.getEntries().forEach(regEntry -> {

            String laType = (String) regEntry.getItem().getFields().get(LOCAL_AUTHORITY_TYPE);
            Item localAuthorityItem = regEntry.getItem();
            String localAuthorityKey = (String) regEntry.getItem().getFields().get(LOCAL_AUTHORITY_ENG);
            ;

            if (stateMap.containsKey(laType)) {
                Collection<Item> currentLocalAuthorities = stateMap.get(laType).getRecord().orElseThrow(IllegalStateException::new).getItems();

                if (!localAuthoritiesContainsKey(currentLocalAuthorities, localAuthorityKey)) {
                    findEntityByAuthority(localAuthorityKey, state)
                            .ifPresent(pe -> {
                                Collection<Item> localAuthList = pe.getRecord().orElseThrow(IllegalStateException::new).getItems();
                                DerivationEntry localAuthoritiesEntryWithRemoved = createLocalAuthoritiesEntryRemoving(localAuthorityKey, localAuthList, entryNumber);
                                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
                            });
                    DerivationEntry newLocalAuthoritiesEntry = createLocalAuthoritiesEntryAdding(localAuthorityItem, currentLocalAuthorities, entryNumber);
                    stateMap.get(laType).getEntries().add(newLocalAuthoritiesEntry);
                }
            } else {
                stateMap.put(laType, createLaTypeEntity(laType, localAuthorityItem, entryNumber));
            }
        }));

        return new HashSet<>(stateMap.values());
    }

    private boolean localAuthoritiesContainsKey(Collection<Item> localAuths, String key) {
        return localAuths.stream().anyMatch(i -> key.equals(i.getFields().get(LOCAL_AUTHORITY_ENG)));
    }

    private Optional<PartialEntity<DerivationEntry>> findEntityByAuthority(String localAuthKey, Collection<PartialEntity<DerivationEntry>> state) {
        return state.stream()
                .filter(e -> localAuthoritiesContainsKey(e.getRecord().orElseThrow(IllegalStateException::new).getItems(), localAuthKey))
                .findFirst();

    }

    private int currentMaxEntryNumber(Collection<PartialEntity<DerivationEntry>> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }

    private DerivationEntry createLocalAuthoritiesEntryAdding(Item localAuthority, Collection<Item> currentLocalAuthorities, AtomicInteger entryNumber) {
        Collection<Item> updatedLocalAuthorities = new HashSet<>(currentLocalAuthorities);
        updatedLocalAuthorities.add(localAuthority);
        return createLocalAuthoritiesEntry(updatedLocalAuthorities, entryNumber);
    }

    private DerivationEntry createLocalAuthoritiesEntryRemoving(String localAuthKey, Collection<Item> currentLocalAuthorities, AtomicInteger entryNumber) {
        Collection<Item> newItems = currentLocalAuthorities.stream()
                .filter( i -> !localAuthKey.equals(i.getFields().get(LOCAL_AUTHORITY_ENG))).collect(toSet());
        return createLocalAuthoritiesEntry(newItems, entryNumber);
    }

    private DerivationEntry createLocalAuthoritiesEntry(Collection<Item> newLocalAuthorities, AtomicInteger entryNumber) {
        DerivationEntry newLocalAuthoritiesEntry = new DerivationEntry(entryNumber.getAndIncrement(), Instant.now());
        newLocalAuthoritiesEntry.getItems().addAll(newLocalAuthorities);
        return newLocalAuthoritiesEntry;
    }

    private PartialEntity<DerivationEntry> createLaTypeEntity(String laType, Item localAuth, AtomicInteger entryNumber) {
        PartialEntity laTypeEntity = new PartialEntity<DerivationEntry>(laType);
        DerivationEntry entry = new DerivationEntry(entryNumber.getAndIncrement(), Instant.now());
        entry.getItems().add(localAuth);
        laTypeEntity.getEntries().add(entry);
        return laTypeEntity;
    }


}
