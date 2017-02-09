package uk.gov.register.derivation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Item {

    private final Map<String,Object> fields;

    public Item(String itemJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
        fields = mapper.readValue(itemJson, typeRef);
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
