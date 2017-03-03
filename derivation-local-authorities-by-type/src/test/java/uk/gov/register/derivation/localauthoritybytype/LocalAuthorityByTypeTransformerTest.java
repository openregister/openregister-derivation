package uk.gov.register.derivation.localauthoritybytype;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LocalAuthorityByTypeTransformerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void shouldTransformSingleEntry() throws Exception {
        Set<PartialEntity<Entry>> update = Collections.singleton(createEntity("LEE", "NMD", "Lewes"));
        Set<PartialEntity<DerivationEntry>> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);
        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        Item item = newStateEntity1.getEntries().get(0).getItems().iterator().next();
        assertThat(item.getFields().get("local-authority-type"), is("NMD"));
        String localAuthority = (String) item.getFields().get("local-authority-eng");
        assertThat(localAuthority, is("LEE"));
    }

    @Test
    public void shouldTransformMultipleEntriesWithSameType() throws Exception {

        Set<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("LEE", "NMD", "Lewes"));
        update.add(createEntity("HAS", "NMD", "Hastings"));

        Collection<PartialEntity<DerivationEntry>> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(2));
        Item item = newStateEntity1.getEntries().get(0).getItems().iterator().next();
        assertThat(item.getFields().get("local-authority-type"), is("NMD"));
        //
        Set<Item> items = newStateEntity1.getEntries().get(1).getItems();
        assertThat(items.size(), is(2));
        List<String> localAuthorities1 = items.stream().map(i -> (String) (i.getFields().get("local-authority-eng")))
                .collect(toList());
        assertThat(localAuthorities1, hasItems("LEE", "HAS"));
    }

    @Test
    public void shouldUpdateCurrentState() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("HAS", "NMD", "Hastings"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD", "Lewes"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);
        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();

        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(2));
        assertThat(newStateEntity1.getEntries().get(0).getItems().iterator().next().getFields().get("local-authority-type"), is("NMD"));
        assertThat(newStateEntity1.getEntries().get(0).getItems().iterator().next().getFields().get("local-authority-eng"), is("LEE"));

        Set<Item> items = newStateEntity1.getEntries().get(1).getItems();
        List<String> localAuthorities1 = items.stream().map(i -> (String) (i.getFields().get("local-authority-eng")))
                .collect(toList());
        assertThat(localAuthorities1, hasItems("LEE", "HAS"));
    }

    @Test
    public void shouldUpdateCurrentStateWithMultipleTypes() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("ESS", "CTY", "Essex"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD", "Lewes"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        assertThat(newState.size(), is(2));

        Map<String, PartialEntity<DerivationEntry>> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity<DerivationEntry> nmdEntity = entityMap.get("NMD");
        assertThat(nmdEntity.getKey(), is("NMD"));
        assertThat(nmdEntity.getEntries().size(), is(1));

        assertThat(nmdEntity.getEntries().get(0).getItems().iterator().next().getFields().get("local-authority-type"), is("NMD"));
        Item item = nmdEntity.getEntries().get(0).getItems().iterator().next();
        assertThat(item.getFields().get("local-authority-eng"), is("LEE"));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity<DerivationEntry> ctyEntity = entityMap.get("CTY");
        assertThat(ctyEntity.getKey(), is("CTY"));
        assertThat(ctyEntity.getEntries().size(), is(1));

        Item item2 = nmdEntity.getEntries().get(0).getItems().iterator().next();
        assertThat(item2.getFields().get("local-authority-eng"), is("LEE"));
    }

    @Test
    public void shouldIgnoreChangeNotAffectingType() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("LEE", "NMD", "New Lewes"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD", "Lewes"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
    }

    @Test
    public void shouldAddAndRemoveAuthoritiesWhenTypeChanges() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("ESS", "NMD", "Essex"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD", "Lewes"));
        state.add(createDerivationEntity("ESS", "CTY", "Essex"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        assertThat(newState.size(), is(2));

        Map<String, PartialEntity<DerivationEntry>> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity<DerivationEntry> nmdEntity = entityMap.get("NMD");
        assertThat(nmdEntity.getKey(), is("NMD"));
        assertThat(nmdEntity.getEntries().size(), is(2));

        List<String> localAuthNames = nmdEntity.getEntries().get(1).getItems().stream().map(i -> (String) i.getFields()
                .get("local-authority-eng")).collect(Collectors.toList());

        assertThat(localAuthNames, hasItems("LEE", "ESS"));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity<DerivationEntry> ctyEntity = entityMap.get("CTY");
        assertThat(ctyEntity.getKey(), is("CTY"));
        assertThat(ctyEntity.getEntries().size(), is(2));

        List<String> localAuthNamesCty = ctyEntity.getEntries().get(1).getItems().stream().map(i -> (String) i.getFields()
                .get("local-authority-eng")).collect(Collectors.toList());

        assertThat(localAuthNames, hasItems("LEE"));
    }

    private PartialEntity<DerivationEntry> createDerivationEntity(String localAuthorityKey, String localAuthorityTypeKey, String name) {
        PartialEntity<DerivationEntry> partialEntity = new PartialEntity<>(localAuthorityTypeKey);
        Item item = getItem(localAuthorityKey, localAuthorityTypeKey, name);
        DerivationEntry entry = new DerivationEntry(1, Instant.EPOCH);
        entry.getItems().add(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private PartialEntity<Entry> createEntity(String key, String authorityType, String name) {
        PartialEntity partialEntity = new PartialEntity(key);
        Item item = getItem(key, authorityType, name);
        Entry entry = new Entry(1, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private Item getItem(String key, String authorityType, String name) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-eng", key);
        fields.put("local-authority-type", authorityType);
        fields.put("name", name);
        return new Item(fields);
    }

}
