package uk.gov.register.derivation.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;


public class RsfParser {

    private final static String TAB = "\t";

    private final static String ADD_ITEM = "add-item";
    private final static String APPEND_ENTRY = "append-entry";
    private final static String ASSERT_ROOT_HASH = "assert-root-hash";

    private static ObjectMapper MAPPER = new ObjectMapper();
    private static TypeReference<HashMap<String,Object>> TYPEREF = new TypeReference<HashMap<String,Object>>() {};

    public Set<PartialEntity> parse(InputStream rsfStream) {
        final Map<String, Item> itemsByHash = new HashMap<>();
        final Map<String, List<Entry>> entriesByKey = new HashMap<>();
        AtomicInteger entryNumber = new AtomicInteger(startingEntryNumber());
        BufferedReader buffer = new BufferedReader(new InputStreamReader(rsfStream));
        buffer.lines().forEach(line -> parseLine(line, itemsByHash, entriesByKey, entryNumber));
        return collectToEntity(itemsByHash, entriesByKey);

    }

    private Set<PartialEntity> collectToEntity(Map<String, Item> itemsByHash, Map<String, List<Entry>> entriesByKey) {
        final Map<String, PartialEntity> entities = new HashMap<>();

        entriesByKey.entrySet().stream().forEach(kv -> {
            List<Entry> entries = kv.getValue().stream().map(entry -> {
                String itemHash = entry.getItemHash();
                if (!itemsByHash.containsKey(itemHash)) {
                    throw new SerializedRegisterParseException("failed to find item matching hash: " + itemHash);
                }
                entry.setItem(itemsByHash.get(itemHash));
                return entry;
            }).collect(Collectors.toList());

            if (!entities.containsKey(kv.getKey())) {
                entities.put(kv.getKey(), new PartialEntity(kv.getKey()));
            }
            entities.get(kv.getKey()).getEntries().addAll(entries);

        });

        return new HashSet<>(entities.values());
    }


    private void parseLine(String line, Map<String, Item> itemsByHash, Map<String, List<Entry>> entriesByKey, AtomicInteger entryNumber) throws SerializedRegisterParseException {
        List<String> parts = Arrays.asList(line.split(TAB));

        String commandName = parts.get(0);

        try {
            if (ADD_ITEM.equals(commandName) && parts.size() == 2) {
                Map<String,Object>  fields = MAPPER.readValue(parts.get(1), TYPEREF);
                Item item = new Item(fields);
                String itemHash = "sha-256:" + sha256Hex(parts.get(1));
                itemsByHash.put(itemHash, item);
            } else if (APPEND_ENTRY.equals(commandName) && parts.size() == 4) {
                Entry entry = new Entry(entryNumber.getAndIncrement(), Instant.parse(parts.get(1)), parts.get(2));
                if (!entriesByKey.containsKey(parts.get(3))) {
                    entriesByKey.put(parts.get(3), new LinkedList<>());
                }
                entriesByKey.get(parts.get(3)).add(entry);

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
        // TODO
        return 1;
    }

}
