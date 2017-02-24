package uk.gov.register.derivation.localauthoritybytype;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

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
        Set<PartialEntity> update = Collections.singleton(createEntity("LEE", "NMD", "Lewes"));
        Set<PartialEntity> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);
        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        assertTrue(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities") instanceof List);
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities.get(0), is("LEE"));
    }

    @Test
    public void shouldTransformMultipleEntriesWithSameType() throws Exception {

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("LEE", "NMD", "Lewes", 1));
        update.add(createEntity("HAS", "NMD", "Hastings", 2));

        Set<PartialEntity> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(2));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthorities1 = (List<String>) newStateEntity1.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities1, hasItems("LEE", "HAS"));
    }

    @Test
    public void shouldUpdateCurrentState() throws Exception {

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("HAS", "NMD", "Hastings"));

        Set<PartialEntity> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);
        PartialEntity newStateEntity1 = newState.iterator().next();
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

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("ESS", "CTY", "Essex"));

        Set<PartialEntity> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        assertThat(newState.size(), is(2));

        Map<String, PartialEntity> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity nmdEntity = entityMap.get("NMD");
        assertThat(nmdEntity.getKey(), is("NMD"));
        assertThat(nmdEntity.getEntries().size(), is(1));
        assertThat(nmdEntity.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthoritiesNmd = (List<String>) nmdEntity.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesNmd, hasItems("LEE"));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity ctyEntity = entityMap.get("CTY");
        assertThat(ctyEntity.getKey(), is("CTY"));
        assertThat(ctyEntity.getEntries().size(), is(1));
        assertThat(ctyEntity.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("CTY"));
        List<String> localAuthoritiesCty = (List<String>) ctyEntity.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesCty, hasItems("ESS"));
    }

    @Test
    public void shouldIgnoreChangeNotAffectingType() throws Exception {

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("LEE", "NMD", "New Lewes"));

        Set<PartialEntity> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        PartialEntity newStateEntity1 = newState.iterator().next();
        assertThat(newStateEntity1.getKey(), is("NMD"));
        assertThat(newStateEntity1.getEntries().size(), is(1));
        assertThat(newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthorities = (List<String>) newStateEntity1.getEntries().get(0).getItem().getFields().get("local-authorities");
        assertThat(localAuthorities, hasItems("LEE"));
    }

    @Test
    public void shouldAddAndRemoveAuthoritiesWhenTypeChanges() throws Exception {

        Set<PartialEntity> state = new HashSet<>();
        state.add(createDerivationEntity("LEE", "NMD"));
        state.add(createDerivationEntity("ESS", "CTY"));

        Set<PartialEntity> update = new HashSet<>();
        update.add(createEntity("ESS", "NMD", "Essex"));

        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        assertThat(newState.size(), is(2));

        Map<String, PartialEntity> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity nmdEntity = entityMap.get("NMD");
        assertThat(nmdEntity.getKey(), is("NMD"));
        assertThat(nmdEntity.getEntries().size(), is(2));
        assertThat(nmdEntity.getEntries().get(1).getItem().getFields().get("local-authority-type"), is("NMD"));
        List<String> localAuthoritiesNmd = (List<String>) nmdEntity.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesNmd, hasItems("LEE","ESS"));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity ctyEntity = entityMap.get("CTY");
        assertThat(ctyEntity.getKey(), is("CTY"));
        assertThat(ctyEntity.getEntries().size(), is(2));
        assertThat(ctyEntity.getEntries().get(1).getItem().getFields().get("local-authority-type"), is("CTY"));
        List<String> localAuthoritiesCty = (List<String>) ctyEntity.getEntries().get(1).getItem().getFields().get("local-authorities");
        assertThat(localAuthoritiesCty, is(empty()));
    }

    @Test
    public void shouldPreserveEntryOrder() throws Exception {
        Set<PartialEntity> update = new HashSet<>();

        PartialEntity leeEntity = new PartialEntity("LEE");
        List<Entry> leeEntries = leeEntity.getEntries();
        update.add(leeEntity);

        PartialEntity hasEntity = new PartialEntity("HAS");
        List<Entry> hasEntries = hasEntity.getEntries();
        update.add(hasEntity);

        PartialEntity wsxEntity = new PartialEntity("WSX");
        List<Entry> wsxEntries = wsxEntity.getEntries();
        update.add(wsxEntity);

        PartialEntity wyeEntity = new PartialEntity("WYE");
        List<Entry> wyeEntries = wyeEntity.getEntries();
        update.add(wyeEntity);

        PartialEntity horEntity = new PartialEntity("HOR");
        List<Entry> horEntries = horEntity.getEntries();
        update.add(horEntity);

        PartialEntity worEntity = new PartialEntity("WOR");
        List<Entry> worEntries = worEntity.getEntries();
        update.add(worEntity);

        PartialEntity wawEntity = new PartialEntity("WAW");
        List<Entry> wawEntries = wawEntity.getEntries();
        update.add(wawEntity);

        // entry 1
        leeEntries.add(createLocalAuthEntry("LEE", "NMD", "Lewes", 1));
        // entry 2
        hasEntries.add( createLocalAuthEntry("HAS", "NMD", "Hastings", 2));
        // entry 3
        wsxEntries.add( createLocalAuthEntry("WSX", "CTY", "West Sussex", 3));
        // entry 4
        wyeEntries.add( createLocalAuthEntry("WYE", "NMD", "Wyre Forest", 4));
        // entry 5
        horEntries.add( createLocalAuthEntry("HOR", "NMD", "Horsham", 5));
        // entry 6
        worEntries.add( createLocalAuthEntry("WOR", "NMD", "Worcester", 6));
        // no entry
        worEntries.add(createLocalAuthEntry("WOR", "NMD", "New Worcester", 7));
        // entry 7
        wawEntries.add( createLocalAuthEntry("WAW", "NMD", "Warwick", 8));
        // entries 8 and 9
        leeEntries.add(createLocalAuthEntry("LEE", "CTY", "Lewes", 9));

        Set<PartialEntity> state = Collections.emptySet();
        LocalAuthorityByTypeTransformer transformer = new LocalAuthorityByTypeTransformer();
        Set<PartialEntity> newState = transformer.transform(update, state);

        Map<String, PartialEntity> entityMap = newState.stream().collect(toMap(PartialEntity::getKey, identity()));
        assertTrue(entityMap.containsKey("NMD"));
        PartialEntity nmdEntity = entityMap.get("NMD");

        List<Entry> nmdEntityEntries = nmdEntity.getEntries();
        assertThat( nmdEntityEntries.get(0).getEntryNumber(), is(1));
        assertThat( nmdEntityEntries.get(1).getEntryNumber(), is(2));
        assertThat( nmdEntityEntries.get(2).getEntryNumber(), is(4));
        assertThat( nmdEntityEntries.get(3).getEntryNumber(), is(5));
        assertThat( nmdEntityEntries.get(4).getEntryNumber(), is(6));
        assertThat( nmdEntityEntries.get(5).getEntryNumber(), is(7));
        assertThat( nmdEntityEntries.get(6).getEntryNumber(), is(8));

        assertTrue(entityMap.containsKey("CTY"));
        PartialEntity ctyEntity = entityMap.get("CTY");
        List<Entry> ctyEntries = ctyEntity.getEntries();
        assertThat( ctyEntries.get(0).getEntryNumber(), is(3));
        assertThat( ctyEntries.get(1).getEntryNumber(), is(9));
    }

    private PartialEntity createDerivationEntity(String localAuthorityKey, String localAuthorityTypeKey) {
        PartialEntity partialEntity = new PartialEntity(localAuthorityTypeKey);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-type", localAuthorityTypeKey);
        List<String> localAuthorityKeys = new LinkedList<>();
        localAuthorityKeys.add(localAuthorityKey);
        fields.put("local-authorities", localAuthorityKeys);
        Item item = new Item(fields);
        Entry entry = new Entry(1, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private PartialEntity createEntity(String key, String authorityType, String name) {
        return createEntity(key, authorityType, name, 1);
    }

    private PartialEntity createEntity(String key, String authorityType, String name, int entryNumber) {
        PartialEntity partialEntity = new PartialEntity(key);
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-eng", key);
        fields.put("local-authority-type", authorityType);
        fields.put("name", name);
        Item item = new Item(fields);
        Entry entry = new Entry(entryNumber, Instant.EPOCH, "sha-256:1");
        entry.setItem(item);
        partialEntity.getEntries().add(entry);
        return partialEntity;
    }

    private Entry createLocalAuthEntry(String key, String authorityType, String name, int entryNumber ){
        Map<String, Object> fields = new HashMap<>();
        fields.put("local-authority-eng", key);
        fields.put("local-authority-type", authorityType);
        fields.put("name", name);
        Item item = new Item(fields);
        Entry entry = new Entry(entryNumber, Instant.EPOCH, "sha-256:zzz");
        entry.setItem(item);
        return entry;
    }

}
