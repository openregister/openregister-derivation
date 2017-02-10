package uk.gov.register.derivation.core;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RsfParserTest {


    private RsfParser parser;

    @Before
    public void setup() {
        parser = new RsfParser();
    }

    @Test
    public void shouldParseEntityWithSingleEntry() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "countries.rsf"));
        Set<PartialEntity> entities = parser.parse(rdfStream);
        System.out.println( entities );
        assertThat(entities.size(), is(3));

        Map<String, PartialEntity> entityMap = getEntityMap(entities);
        assertTrue( entityMap.containsKey("LS"));
        PartialEntity entity = entityMap.get("LS");
        assertThat(entity.getEntries().size(), is(1));
        assertThat(entity.getEntries().get(0).getItem(), is(notNullValue()));
        assertThat(entity.getEntries().get(0).getItem().getFields(), is(notNullValue()) );
        assertTrue(entity.getEntries().get(0).getItem().getFields().containsKey("name") );
        assertThat(entity.getEntries().get(0).getItem().getFields().get("name"), is("Lesotho") );
        assertThat(entity.getEntries().get(0).getItem().getFields().get("citizen-names"), is("Citizen of Lesotho") );
    }

    @Test
    public void latestEntryShouldBeLastInList() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "countries.rsf"));
        Set<PartialEntity> entities = parser.parse(rdfStream);
        System.out.println( entities );
        assertThat(entities.size(), is(3));

        Map<String, PartialEntity> entityMap = getEntityMap(entities);
        assertTrue( entityMap.containsKey("CZ"));
        PartialEntity entity = entityMap.get("CZ");
        assertThat(entity.getEntries().size(), is(2));
        assertThat(entity.getEntries().get(1).getItem(), is(notNullValue()));
        assertThat(entity.getEntries().get(1).getItem().getFields(), is(notNullValue()) );
        assertTrue(entity.getEntries().get(1).getItem().getFields().containsKey("name") );
        assertThat(entity.getEntries().get(1).getItem().getFields().get("name"), is("Czechia") );
        assertThat(entity.getEntries().get(1).getItem().getFields().get("citizen-names"), is("Czech") );
        assertThat(entity.getEntries().get(1).getTimestamp().toString(), is("2016-11-11T16:25:07Z") );
    }

    @Test
    public void shouldFailIfItemsAndEntriesDoNotMatch() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "bad-countries.rsf"));
        try {
            parser.parse(rdfStream);
        } catch (SerializedRegisterParseException e){
            assertThat(e.getMessage(), is("failed to find item matching hash: sha-256:c69c04fff98c59aabd739d43018e87a25fd51a00c37d100721cc68fa9003a720-I-am-wrong"));
        }
    }

    @Test
    public void shouldFailIfInvalidRsf() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "invalid.rsf"));
        try {
            parser.parse(rdfStream);
        } catch (SerializedRegisterParseException e){
            assertThat(e.getMessage(), is("failed to parse line append-entry\t2016-04-05T13:23:05Z\tsha-256:d97d6b34bc572e334cbd7898f785b72947557d9dbea59977077f231274259f3b"));
        }
    }

    private Map<String,PartialEntity> getEntityMap(Set<PartialEntity> entities){
        return entities.stream().collect(Collectors.toMap(PartialEntity::getKey, Function.identity()));
    }

}