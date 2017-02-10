package uk.gov.register.derivation.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String serialize(Object data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}
