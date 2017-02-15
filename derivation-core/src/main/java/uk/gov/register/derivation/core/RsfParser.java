package uk.gov.register.derivation.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class RsfParser {

    private final static String TAB = "\t";

    private static ObjectMapper MAPPER = new ObjectMapper();
    private static TypeReference<HashMap<String, Object>> TYPE_REF = new TypeReference<HashMap<String, Object>>() {
    };

    public Stream<Entry> parse(InputStream rsfStream) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(rsfStream));
        PairBufferedReader pairBufferedReader = new PairBufferedReader(buffer);
        return pairBufferedReader.readPairs().map(this::parsePair);

    }

    private Entry parsePair(String pair) {
        String[] pairArray = pair.split("\n");
        List<String> itemParts = Arrays.asList(pairArray[0].split(TAB));
        List<String> entryParts = Arrays.asList(pairArray[1].split(TAB));
        try {
            Map<String, Object> fields = MAPPER.readValue(itemParts.get(1), TYPE_REF);
            Item item = new Item(fields);
            // TODO fix entry number
            Entry entry = new Entry(999, Instant.parse(entryParts.get(1)), entryParts.get(2));
            entry.setItem(item);
            return entry;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }



}
