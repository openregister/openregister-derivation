package uk.gov.register.derivation.cli;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.PartialEntity;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class JsonSerializerTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void deserialize() throws Exception {
        InputStream inputStream = Files.newInputStream(Paths.get("src/test/resources", "entities.json"));
        Set<PartialEntity> entities = JsonSerializer.deserialize(inputStream, new TypeReference<Set<PartialEntity>>() {
        });
        Map<String, PartialEntity> entityMap = entities.stream().collect(toMap(PartialEntity::getKey, Function.identity()));
        PartialEntity andora = entityMap.get("AD");
        assertNotNull(andora);
        assertThat(andora.getEntries().get(0).getItem().getFields().get("name"), is("Andorra"));
    }

}
