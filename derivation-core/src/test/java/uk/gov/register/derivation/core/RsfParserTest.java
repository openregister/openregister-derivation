package uk.gov.register.derivation.core;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void shouldParseEntry() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "countries.rsf"));
        Stream<Entry> entryStream = parser.parse(rdfStream);
        List<Entry> entries = entryStream.collect(Collectors.toList());
        assertThat(entries.size(), is(4));
        Entry entry0 = entries.get(0);
        assertTrue( entry0.getItem().getFields().containsKey("name"));
        assertThat( entry0.getItem().getFields().get("name"), is("Czech Republic"));
    }



}
