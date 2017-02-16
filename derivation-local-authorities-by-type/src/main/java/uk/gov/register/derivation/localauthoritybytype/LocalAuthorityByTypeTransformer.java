package uk.gov.register.derivation.localauthoritybytype;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;


public class LocalAuthorityByTypeTransformer implements RegisterTransformer {

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, Function.identity()));
        newPartialEntities.forEach(newEntity -> {
            newEntity.getEntries().forEach(entry -> {
                String laType = (String) entry.getItem().getFields().get("local-authority-type");
                String localAuthority = (String) entry.getItem().getFields().get("local-authority-eng");
                if (stateMap.containsKey(laType)) {
                    List<String> currentLocalAuthorities = (List<String>) stateMap.get(laType).getRecord().getItem().getFields().get("local-authority");
                    currentLocalAuthorities.add(localAuthority);
                } else {
                    stateMap.put(laType, createLaTypeEntity(laType, localAuthority));
                }
            });
        });
        return new HashSet<>(stateMap.values());
    }


    private PartialEntity createLaTypeEntity(String laType, String localAuth) {
        PartialEntity laTypeEntity = new PartialEntity(laType);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-type", laType);
        LinkedList<String> localAuthList = new LinkedList<>();
        localAuthList.add(localAuth);
        fields.put("local-authority", localAuthList );
        Item item = new Item(fields);
        Entry entry = new Entry(getSequenceNumber(), Instant.now(), hashValue(localAuth));
        entry.setItem(item);
        laTypeEntity.getEntries().add(entry);
        return laTypeEntity;
    }

    private String hashValue(String localAuth) {
        return "";
    }

    private int getSequenceNumber() {
        // TODO
        return 1;
    }
}
