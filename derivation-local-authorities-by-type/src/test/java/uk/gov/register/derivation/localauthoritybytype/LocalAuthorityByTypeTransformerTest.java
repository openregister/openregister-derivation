package uk.gov.register.derivation.localauthoritybytype;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.time.Instant;
import java.util.*;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class LocalAuthorityByTypeTransformerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldTransformSingleEntry() throws Exception {
        Set<PartialEntity> update = Collections.singleton(createEntity("BAS", "UA"));
        Set<PartialEntity> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);
        assertNotNull(newState);
        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("UA"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("UA"));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority");
        assertThat(localAuthorities.get(0), is("BAS"));
    }

    @Test
    public void shouldTransformMultipleEntries() throws Exception {

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("BAS", "UA"));
        update.add(createEntity("ATM", "UA"));

        Set<PartialEntity> state = Collections.emptySet();
        Set<PartialEntity> expected = Collections.singleton(createDerivationEntity("BAS", "UA"));
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);
        assertNotNull(newState);
        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("UA"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("UA"));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority");
        assertThat(localAuthorities, hasItems("BAS","ATM"));
    }

    @Test
    public void shouldUpdateCurrentState() throws Exception {

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("ATM", "UA"));

        Set<PartialEntity> state = new HashSet<>();
        state.add(createDerivationEntity("BAS", "UA"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);
        assertNotNull(newState);
        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("UA"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("UA"));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority");
        assertThat(localAuthorities, hasItems("BAS","ATM"));
    }

    @Test
    public void shouldUpdateCurrentStateWithMultipleTypes() throws Exception {

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("ATM", "UA"));

        Set<PartialEntity> state = new HashSet<>();
        state.add(createDerivationEntity("BAS", "UA"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);
        assertNotNull(newState);
        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("UA"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("UA"));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority");
        assertThat(localAuthorities, hasItems("BAS","ATM"));
    }

    private PartialEntity createDerivationEntity(String localAuthorityKey, String localAuthorityTypeKey) {
        PartialEntity partialEntity = new PartialEntity(localAuthorityTypeKey);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-type", localAuthorityTypeKey);
        List<String> localAuthorityKeys = new LinkedList<>();
        localAuthorityKeys.add(localAuthorityKey);
        fields.put("local-authority", localAuthorityKeys);
        Item item = new Item(fields);
        Entry entry = new Entry(1, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private PartialEntity createEntity(String key, String authorityType) {
        PartialEntity partialEntity = new PartialEntity(key);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-eng", key);
        fields.put("local-authority-type", authorityType);
        Item item = new Item(fields);
        Entry entry = new Entry(1, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

}
