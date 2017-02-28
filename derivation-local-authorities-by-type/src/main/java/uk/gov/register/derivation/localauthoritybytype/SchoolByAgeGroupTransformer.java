package uk.gov.register.derivation.localauthoritybytype;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.*;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class SchoolByAgeGroupTransformer extends GroupingTransformer {

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, Function.identity()));

        AtomicInteger entryNumber = new AtomicInteger(currentMaxEntryNumber(state));

        DerivationUtils.asEntryLog(newPartialEntities).forEach(keyedEntry -> {

            Integer minAge = (Integer) keyedEntry.entry.getItem().getFields().get("minimum-age");
            Integer maxAge = (Integer) keyedEntry.entry.getItem().getFields().get("maximum-age");
            List<Integer> ageGroups = ageGroups(minAge, maxAge);


            String school = keyedEntry.key;

            for (Integer ageGroup : ageGroups) {
                String ageGroupStr = String.valueOf(ageGroup);
                if (stateMap.containsKey(ageGroupStr)) {
                    PartialEntity ageEntity = stateMap.get(ageGroup);
                    Entry record = ageEntity.getRecord().orElseThrow(RuntimeException::new);
                    if (!servesAge(record, ageGroup)) {
                        Entry newEntry = createAgeGroupEntry(ageGroup, record,  entryNumber.incrementAndGet(), school);
                        ageEntity.getEntries().add(newEntry);
                        // now remove
                        Set<PartialEntity> entitesByAgeAgeGroup =  findEntitiesByAgeGroup( ageGroup );

                    }
                } else {
                    PartialEntity ageEntity = new PartialEntity(ageGroupStr);
                    Entry newEntry = createAgeGroupEntry(ageGroup, entryNumber.incrementAndGet(), Collections.emptyList(), school);
                    ageEntity.getEntries().add(newEntry);
                    stateMap.put(ageGroupStr, ageEntity);
                }
            }
        });

        return new HashSet<>(stateMap.values());
    }

    private Set<PartialEntity> findEntitiesByAgeGroup(Integer ageGroup) {
        //TODO
        return null;
    }

    private Entry createAgeGroupEntry(Integer ageGroup, Entry record, int entryNumber, String school) {
        List<String> currentList = (List<String>)record.getItem().getFields().get("schools");
        return createAgeGroupEntry(ageGroup, entryNumber, currentList, school);
    }

    private Entry createAgeGroupEntry(Integer ageGroup, int entryNumber, List<String> currentList, String school) {
        List<String> newList = new ArrayList<>(currentList);
        newList.add(school);
        Map<String, Object> fields = new HashMap<>();
        fields.put("age-group", ageGroup);
        fields.put("schools", newList);
        Item item = new Item(fields);
        Entry entry = new Entry(entryNumber, Instant.now(), "sha-256:z");
        entry.setItem(item);
        return entry;
    }

    private boolean servesAge(Entry record, Integer ageGroup) {
        return ((List<Integer>) record.getItem().getFields().get("age-groups")).contains(ageGroup);
    }

    private List<Integer> ageGroups(Integer minAge, Integer maxAge) {
        return IntStream.range(minAge, maxAge + 1).boxed().collect(Collectors.toList());

    }

    String groupingField() {
        return "age-groups";
    }

    String keyListField() {
        return "school";
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
