package uk.gov.register.derivation;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;

public class RsfSerializerTest {
    @Test
    public void shouldSerializePartialEntitiesAsJson() throws IOException, JSONException {
        PartialEntity entity = new PartialEntity("CZ");
        Entry entry1 = new Entry(1, Instant.parse("2016-11-11T16:25:07Z"), "sha-256:c45bd0b4785680534e07c627a5eea0d2f065f0a4184a02ba2c1e643672c3f2ed");
        Entry entry2 = new Entry(2, Instant.parse("2016-11-11T16:25:07Z"), "sha-256:c69c04fff98c59aabd739d43018e87a25fd51a00c37d100721cc68fa9003a720");
        entry1.setItem(new Item("{\"citizen-names\":\"Czech\",\"country\":\"CZ\",\"name\":\"Czech Republic\",\"official-name\":\"The Czech Republic\",\"start-date\":\"1993-01-01\"}"));
        entry2.setItem(new Item("{\"citizen-names\":\"Czech\",\"country\":\"CZ\",\"name\":\"Czechia\",\"official-name\":\"The Czech Republic\",\"start-date\":\"1993-01-01\"}"));
        entity.getEntries().addAll(Arrays.asList(entry1, entry2));

        String result = JsonSerializer.serialize(entity);

        String expectedJson = "{\n" +
                "  \"key\": \"CZ\",\n" +
                "  \"entries\": [\n" +
                "    {\n" +
                "      \"itemHash\": \"sha-256:c45bd0b4785680534e07c627a5eea0d2f065f0a4184a02ba2c1e643672c3f2ed\",\n" +
                "      \"sequenceNumber\": 1,\n" +
                "      \"item\": {\n" +
                "        \"fields\": {\n" +
                "          \"country\": \"CZ\",\n" +
                "          \"official-name\": \"The Czech Republic\",\n" +
                "          \"name\": \"Czech Republic\",\n" +
                "          \"start-date\": \"1993-01-01\",\n" +
                "          \"citizen-names\": \"Czech\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"timestamp\": \"2016-11-11T16:25:07Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"itemHash\": \"sha-256:c69c04fff98c59aabd739d43018e87a25fd51a00c37d100721cc68fa9003a720\",\n" +
                "      \"sequenceNumber\": 2,\n" +
                "      \"item\": {\n" +
                "        \"fields\": {\n" +
                "          \"country\": \"CZ\",\n" +
                "          \"official-name\": \"The Czech Republic\",\n" +
                "          \"name\": \"Czechia\",\n" +
                "          \"start-date\": \"1993-01-01\",\n" +
                "          \"citizen-names\": \"Czech\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"timestamp\": \"2016-11-11T16:25:07Z\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        JSONAssert.assertEquals(expectedJson, result, false);
    }
}
