package uk.gov.register.derivation.core;

import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RsfParserTest {


    private RsfParser parser;

    @Before
    public void setup() {
        parser = new RsfParser();
    }

    @Test
    public void shouldParseEntryWhenCorrectItemIsSpecified() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "countries.rsf"));
        Entry entry = parser.parse(rdfStream);

        assertThat(entry.getItemHash(), is("sha-256:c45bd0b4785680534e07c627a5eea0d2f065f0a4184a02ba2c1e643672c3f2ed"));
    }

    @Test
    public void shouldFailIfItemAndEntryDoNotMatch() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "bad-countries.rsf"));
        try {
            parser.parse(rdfStream);
        } catch (SerializedRegisterParseException e){
            assertThat(e.getMessage(), is("Item does not match that specified in entry: sha-256:c69c04fff98c59aabd739d43018e87a25fd51a00c37d100721cc68fa9003a720-I-am-wrong"));
        }
    }

    @Test
    public void shouldFailIfItemAndEntryAreNotInCorrectOrder() throws Exception {
        InputStream rdfStream = Files.newInputStream(Paths.get("src/test/resources", "bad-order.rsf"));
        try {
            parser.parse(rdfStream);
        } catch (SerializedRegisterParseException e){
            assertThat(e.getMessage(), is("Item has not been loaded"));
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
}
