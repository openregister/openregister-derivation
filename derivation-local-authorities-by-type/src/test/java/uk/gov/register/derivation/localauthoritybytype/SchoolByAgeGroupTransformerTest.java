package uk.gov.register.derivation.localauthoritybytype;

import org.junit.Test;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SchoolByAgeGroupTransformerTest {

    @Test
    public void shouldTransformSingleEntry() throws Exception {
        Set<PartialEntity> update = Collections.singleton(createEntity("100008", 3, 11, "Argyle Primary"));
        Set<PartialEntity> state = Collections.emptySet();

        SchoolByAgeGroupTransformer transformer = new SchoolByAgeGroupTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        final Map<String, PartialEntity> stateMap = newState.stream().collect(toMap(PartialEntity::getKey, Function.identity()));

        PartialEntity newStateEntity1 = stateMap.get("11");
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("schools") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("schools");
        assertThat(localAuthorities.get(0), is("100008"));
    }

    @Test
    public void shouldTransformWhenAgeRangeChanges() throws Exception {
        Set<PartialEntity> update = Collections.singleton(createEntity("100008", 3, 11, "Argyle Primary"));
        Set<PartialEntity> state = Collections.singleton(createAgeRangeEntity("13", "100008", 1));

        SchoolByAgeGroupTransformer transformer = new SchoolByAgeGroupTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        final Map<String, PartialEntity> stateMap = newState.stream().collect(toMap(PartialEntity::getKey, Function.identity()));

        PartialEntity newStateEntity1 = stateMap.get("11");
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("schools") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("schools");
        assertThat(localAuthorities.get(0), is("100008"));

        PartialEntity newStateEntity13 = stateMap.get("13");
        assertThat(newStateEntity1.getEntries().size(), is(2));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("schools") instanceof List);
        //List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("schools");
        //assertThat(localAuthorities.get(0), is("100008"));
    }

    private PartialEntity createEntity(String key, Integer minimumAge, Integer maximumAge, String name) {
        return createEntity(key, minimumAge, maximumAge, name, 1);
    }

    private PartialEntity createEntity(String key, Integer minimumAge, Integer maximumAge, String name, int entryNumber) {
        PartialEntity partialEntity = new PartialEntity(key);
        Map<String, Object> fields = new HashMap<>();
        fields.put("school", key);
        fields.put("minimum-age", minimumAge);
        fields.put("maximum-age", maximumAge);
        fields.put("name", name);
        Item item = new Item(fields);
        Entry entry = new Entry(entryNumber, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private PartialEntity createAgeRangeEntity(String key, String schoolId, int entryNumber) {
        PartialEntity partialEntity = new PartialEntity(key);
        Map<String, Object> fields = new HashMap<>();
        fields.put("age-range", key);
        List<String> schools = new ArrayList<>();
        schools.add(schoolId);
        fields.put("schools", schools);
        Item item = new Item(fields);
        Entry entry = new Entry(entryNumber, Instant.EPOCH, "sha-256:zzz");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }


}
