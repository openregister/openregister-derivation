package uk.gov.register.derivation.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PartialEntityTest {
    @Test
    public void shouldSerializePartialEntitiesAsJson() throws IOException, JSONException {
        PartialEntity entity = new PartialEntity("CZ");
        Entry entry1 = new Entry(1, Instant.parse("2016-11-11T16:25:07Z"), "sha-256:1");
        Entry entry2 = new Entry(2, Instant.parse("2016-11-11T16:25:07Z"), "sha-256:2");

        Map<String, Object> fields1 = new HashMap<>();
        fields1.put("country", "CZ");
        fields1.put("name", "Czech Republic");

        Map<String, Object> fields2 = new HashMap<>();
        fields2.put("country", "CZ");
        fields2.put("name", "Czechia");

        entry1.setItem(new Item(fields1));
        entry2.setItem(new Item(fields2));
        entity.getEntries().addAll(Arrays.asList(entry1, entry2));

        ObjectMapper objectMapper = new ObjectMapper();

        String result = objectMapper.writeValueAsString(entity);

        String expectedJson = "{" +
                "  \"key\": \"CZ\"," +
                "  \"entries\": [" +
                "    {" +
                "      \"itemHash\": \"sha-256:1\"," +
                "      \"entryNumber\": 1," +
                "      \"item\": {" +
                "        \"fields\": {" +
                "          \"country\": \"CZ\"," +
                "          \"name\": \"Czech Republic\"" +
                "        }" +
                "      }," +
                "      \"timestamp\": \"2016-11-11T16:25:07Z\"" +
                "    }," +
                "    {" +
                "      \"itemHash\": \"sha-256:2\"," +
                "      \"entryNumber\": 2," +
                "      \"item\": {" +
                "        \"fields\": {" +
                "          \"country\": \"CZ\"," +
                "          \"name\": \"Czechia\"" +
                "        }" +
                "      }," +
                "      \"timestamp\": \"2016-11-11T16:25:07Z\"" +
                "    }" +
                "  ]" +
                "}";

        JSONAssert.assertEquals(expectedJson, result, false);
    }
}
