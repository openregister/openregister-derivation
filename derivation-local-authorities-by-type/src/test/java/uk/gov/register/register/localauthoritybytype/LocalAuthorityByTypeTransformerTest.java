package uk.gov.register.localauthoritybytype;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.localauthoritybytype.LocalAuthorityByTypeTransformer;

import java.time.Instant;
import java.util.*;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.empty;
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
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities.get(0), is("LEE"));
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
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthorities1 = (List<String>) newStateEntity1.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities1, hasItems("LEE", "HAS"));
    }

    @Test
    public void shouldUpdateCurrentState() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("HAS", "NMD", "Hastings"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);
        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(2));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthorities0 = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities0, hasItems("LEE"));
        List<String> localAuthorities1 = (List<String>) newStateEntity1.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities1, hasItems("LEE", "HAS"));
    }

    @Test
    public void shouldUpdateCurrentStateWithMultipleTypes() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("ESS", "CTY", "Essex"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        assertThat(newState.size(), is(2));

        Map<String, PartialEntity<DerivationEntry>> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity<DerivationEntry> nmdEntity = entityMap.get("NMD");
        assertThat(nmdEntity.getKey(), is("NMD"));
        assertThat(nmdEntity.getEntries().size(), is(1));
        assertThat(nmdEntity.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthoritiesNmd = (List<String>) nmdEntity.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesNmd, hasItems("LEE"));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity<DerivationEntry> ctyEntity = entityMap.get("CTY");
        assertThat(ctyEntity.getKey(), is("CTY"));
        assertThat(ctyEntity.getEntries().size(), is(1));
        assertThat(ctyEntity.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("CTY"));
        List<String> localAuthoritiesCty = (List<String>) ctyEntity.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesCty, hasItems("ESS"));
    }

    @Test
    public void shouldIgnoreChangeNotAffectingType() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("LEE", "NMD", "New Lewes"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities, hasItems("LEE"));
    }

    @Test
    public void shouldAddAndRemoveAuthoritiesWhenTypeChanges() throws Exception {

        Collection<PartialEntity<Entry>> update = new HashSet<>();
        update.add(createEntity("ESS", "NMD", "Essex"));

        Collection<PartialEntity<DerivationEntry>> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));
        state.add(createDerivationEntity("ESS", "CTY"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);

        assertThat(newState.size(), is(2));

        Map<String, PartialEntity<DerivationEntry>> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity<DerivationEntry> nmdEntity = entityMap.get("NMD");
        assertThat(nmdEntity.getKey(), is("NMD"));
        assertThat(nmdEntity.getEntries().size(), is(2));
        assertThat(nmdEntity.getEntries().get(1).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthoritiesNmd = (List<String>) nmdEntity.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesNmd, hasItems("LEE","ESS"));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity<DerivationEntry> ctyEntity = entityMap.get("CTY");
        assertThat(ctyEntity.getKey(), is("CTY"));
        assertThat(ctyEntity.getEntries().size(), is(2));
        assertThat(ctyEntity.getEntries().get(1).getItem().getFields().get("local-authority-type"), is("CTY"));
        List<String> localAuthoritiesCty = (List<String>) ctyEntity.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesCty, is(empty()));
    }

    @Test
    public void shouldComputeItemHash(){
        Collection<PartialEntity<Entry>> update = Collections.singleton(createEntity("LEE", "NMD", "Lewes"));
        Collection<PartialEntity<DerivationEntry>> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Collection<PartialEntity<DerivationEntry>> newState = transformer.transform(update, state);
        PartialEntity<DerivationEntry> newStateEntity1 = newState.iterator().next();

        // fields must be in alphabetical order and no whitespace
        String expected = "sha-256:" + DigestUtils.sha256Hex("{\"local-authorities\":[\"LEE\"],\"local-authority-type\":\"NMD\"}");
        String itemHash = newStateEntity1.getEntries().get(0).getItemHash();

        assertThat(itemHash, is(expected));
    }

    private PartialEntity<DerivationEntry> createDerivationEntity(String localAuthorityKey, String localAuthorityTypeKey) {
        PartialEntity partialEntity = new PartialEntity(localAuthorityTypeKey);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-type", localAuthorityTypeKey);
        List<String> localAuthorityKeys = new LinkedList<>();
        localAuthorityKeys.add(localAuthorityKey);
        fields.put("local-authorities", localAuthorityKeys);
        Item item = new Item(fields);
        DerivationEntry entry = new DerivationEntry(1, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private PartialEntity<Entry> createEntity(String key, String authorityType, String name) {
        PartialEntity partialEntity = new PartialEntity(key);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-eng", key);
        fields.put("local-authority-type", authorityType);
        fields.put("name", name);
        Item item = new Item(fields);
        Entry entry = new Entry(1, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

}
