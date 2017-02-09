package uk.gov.register.derivation;

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
import static org.junit.Assert.*;

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
        assertThat(entityMap.get("LS").getEntries().size(), is(1));
        assertThat(entityMap.get("LS").getEntries().get(0).getItem(), is(notNullValue()));
        assertThat(entityMap.get("LS").getEntries().get(0).getItem().getFields(), is(notNullValue()) );
        assertTrue(entityMap.get("LS").getEntries().get(0).getItem().getFields().containsKey("name") );
        assertThat(entityMap.get("LS").getEntries().get(0).getItem().getFields().get("name"), is("Lesotho") );
        assertThat(entityMap.get("LS").getEntries().get(0).getItem().getFields().get("citizen-names"), is("Citizen of Lesotho") );
    }

    private Map<String,PartialEntity> getEntityMap(Set<PartialEntity> entities){
        return entities.stream().collect(Collectors.toMap(PartialEntity::getKey, Function.identity()));
    }

}