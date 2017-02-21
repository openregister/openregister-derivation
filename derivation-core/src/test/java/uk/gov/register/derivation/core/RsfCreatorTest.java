package uk.gov.register.derivation.core;

import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RsfCreatorTest {
    @Test
    public void shouldSerializeCountries() throws IOException {
        Map<String, Object> fieldsForCZOld = new HashMap<>();
        fieldsForCZOld.put("country", "CZ");
        fieldsForCZOld.put("name", "Czech Republic");

        Map<String, Object> fieldsForCZNew = new HashMap<>();
        fieldsForCZNew.put("country", "CZ");
        fieldsForCZNew.put("name", "Czechia");

        Map<String, Object> fieldsForGB = new HashMap<>();
        fieldsForGB.put("country", "GB");
        fieldsForGB.put("name", "United Kingdom");

        Item czOldItem = new Item(fieldsForCZOld);
        Item czNewItem = new Item(fieldsForCZNew);
        Item gbItem = new Item(fieldsForGB);

        Entry czOldEntry = new Entry(1, Instant.now(), "sha-256:cz1");
        Entry gbEntry = new Entry(2, Instant.now(), "sha-256:gb1");
        Entry czNewEntry = new Entry(3, Instant.now(), "sha-256:cz2");

        czOldEntry.setItem(czOldItem);
        czNewEntry.setItem(czNewItem);
        gbEntry.setItem(gbItem);

        PartialEntity czEntity = new PartialEntity("CZ");
        czEntity.getEntries().addAll(Arrays.asList(czOldEntry, czNewEntry));

        PartialEntity gbEntity = new PartialEntity("GB");
        gbEntity.getEntries().add(gbEntry);

        Set<PartialEntity> transformedEntities = new HashSet<>(Arrays.asList(czEntity, gbEntity));

        RsfCreator creator = new RsfCreator();
        String rsf = creator.serializeAsRsf(transformedEntities);

        String[] lines = rsf.split("\n");
        assertThat(lines.length, is(6));
    }

    @Test
    public void shouldSerializeLocalAuthorities() throws IOException {
        Map<String, Object> fieldsForOldNMD = new HashMap<>();
        fieldsForOldNMD.put("local-authority-type", "NMD");
        fieldsForOldNMD.put("local-authority", "[LEE,EES]");

        Map<String, Object> fieldsForCTY = new HashMap<>();
        fieldsForCTY.put("local-authority-type", "CTY");
        fieldsForCTY.put("local-authority", "[HFS]");

        Map<String, Object> fieldsForNewNMD = new HashMap<>();
        fieldsForNewNMD.put("local-authority-type", "NMD");
        fieldsForNewNMD.put("local-authority", "[LEE,EES,LBO]");

        Item nmdOldItem = new Item(fieldsForOldNMD);
        Item nmdNewItem = new Item(fieldsForNewNMD);
        Item ctyItem = new Item(fieldsForCTY);

        Entry nmdOldEntry = new Entry(1, Instant.now(), "sha-256:nmd1");
        Entry ctyEntry = new Entry(2, Instant.now(), "sha-256:cty1");
        Entry nmdNewEntry = new Entry(3, Instant.now(), "sha-256:nmd2");

        nmdOldEntry.setItem(nmdOldItem);
        ctyEntry.setItem(ctyItem);
        nmdNewEntry.setItem(nmdNewItem);

        PartialEntity nmdEntity = new PartialEntity("NMD");
        nmdEntity.getEntries().addAll(Arrays.asList(nmdOldEntry, nmdNewEntry));

        PartialEntity ctyEntity = new PartialEntity("CTY");
        ctyEntity.getEntries().add(ctyEntry);

        Set<PartialEntity> transformedEntities = new HashSet<>(Arrays.asList(ctyEntity, nmdEntity));

        RsfCreator creator = new RsfCreator();
        String rsf = creator.serializeAsRsf(transformedEntities);

        String[] lines = rsf.split("\n");
        assertThat(lines.length, is(6));
    }

    @Test
    public void shouldCanonicalizeJson() {
        Map<String, Object> fieldsForGB = new HashMap<>();
        fieldsForGB.put("name", "United Kingdom");
        fieldsForGB.put("country", "GB");
        Item gbItem = new Item(fieldsForGB);
        Entry gbEntry = new Entry(2, Instant.now(), "sha-256:gb1");
        gbEntry.setItem(gbItem);
        PartialEntity gbEntity = new PartialEntity("GB");
        gbEntity.getEntries().add(gbEntry);

        RsfCreator creator = new RsfCreator();
        String rsf = creator.serializeAsRsf(Collections.singleton(gbEntity));

        String[] lines = rsf.split("\n");
        assertThat(lines.length, is(2));
        String itemJson = lines[0].split("\t")[1];
        assertThat( itemJson, is("{\"country\":\"GB\",\"name\":\"United Kingdom\"}"));
    }
}
