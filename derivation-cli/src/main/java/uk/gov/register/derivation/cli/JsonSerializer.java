package uk.gov.register.derivation.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class JsonSerializer {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String serialize(Object data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
    public static <T> T deserialize(InputStream data, TypeReference<T> type) throws IOException {
        return objectMapper.readValue(data, type);
    }
}
