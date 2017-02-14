package uk.gov.register.derivation.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;


public class RsfParser {
    private final static String TAB = "\t";
    private final static String ADD_ITEM = "add-item";
    private final static String APPEND_ENTRY = "append-entry";
    private final static String ASSERT_ROOT_HASH = "assert-root-hash";

    private static ObjectMapper MAPPER = new ObjectMapper();
    private static TypeReference<HashMap<String,Object>> TYPEREF = new TypeReference<HashMap<String,Object>>() {};

    private AtomicInteger entryNumber = new AtomicInteger(startingEntryNumber());

    private Map.Entry<String, Item> parsedItem;
    private Entry parsedEntry;

    public Entry parse(InputStream lines) {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(lines));
        buffer.lines().forEach(line -> parseLine(line));

        return parsedEntry;
    }

    private void parseLine(String line) throws SerializedRegisterParseException {
        List<String> parts = Arrays.asList(line.split(TAB));

        String commandName = parts.get(0);

        try {
            if (ADD_ITEM.equals(commandName) && parts.size() == 2) {
                Map<String,Object> fields = MAPPER.readValue(parts.get(1), TYPEREF);
                Item item = new Item(fields);
                String itemHash = "sha-256:" + sha256Hex(parts.get(1));
                parsedItem = new AbstractMap.SimpleEntry<>(itemHash, item);
            } else if (APPEND_ENTRY.equals(commandName) && parts.size() == 4) {
                if (parsedItem == null) {
                    throw new SerializedRegisterParseException("Item has not been loaded");
                }

                if (!parts.get(2).equals(parsedItem.getKey())) {
                    throw new SerializedRegisterParseException("Item does not match that specified in entry: " + parts.get(2));
                }

                parsedEntry = new Entry(entryNumber.getAndIncrement(), Instant.parse(parts.get(1)), parts.get(2));
                parsedEntry.setItem(parsedItem.getValue());
            } else if (ASSERT_ROOT_HASH.equals(commandName)) {
                // TODO
                // do nothing for now
                ;
            } else {
                throw new SerializedRegisterParseException("failed to parse line " + line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int startingEntryNumber() {
        return 1;
    }
}
