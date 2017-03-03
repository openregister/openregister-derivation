package uk.gov.register.derivation.web.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DataViews {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Map<String, Map<String, Object>> recordsAsMap(Collection<PartialEntity<Entry>> entities) {
        return entities.stream().collect(toMap(PartialEntity::getKey, DataViews::recordAsMap));
    }

    public static Map<String, Map<String, Object>> derivationRecordsAsMap(Collection<PartialEntity<DerivationEntry>> entities) {
        return entities.stream().collect(toMap(PartialEntity::getKey, DataViews::derivationRecordAsMap));
    }

    public static Map<String, Object> recordAsMap(PartialEntity<Entry> entity) {
        Map<String, Object> combinedItemEntryMap = new HashMap<>();
        Entry record = entity.getRecord().orElseThrow(IllegalStateException::new);
        combinedItemEntryMap.putAll(record.getItem().getFields());
        combinedItemEntryMap.putAll(entryAsMap(record));
        return combinedItemEntryMap;
    }

    private static Map<String, Object> entryAsMap(Entry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("entry-timestamp", entry.getTimestampAsString());
        map.put("entry-number", entry.getEntryNumber());
        map.put("item-hash", entry.getItemHash());
        return map;
    }

    public static List<Map<String, Object>> entriesAsArray(PartialEntity<Entry> partialEntity) {
        return partialEntity.getEntries().stream().map(DataViews::entryAsMap).collect(toList());
    }

    public static Map<String, Object> derivationRecordAsMap(PartialEntity<DerivationEntry> entity) {
        Map<String, Object> combinedItemEntryMap = new HashMap<>();
        DerivationEntry entry = entity.getRecord().orElseThrow(IllegalStateException::new);
        combinedItemEntryMap.putAll(derivationEntryAsMap(entry));
        List<Map<String, Object>> items = entry.getItems().stream().map(Item::getFields).collect(toList());
        combinedItemEntryMap.put("items", items);
        return combinedItemEntryMap;
    }

    private static Map<String, Object> derivationEntryAsMap(DerivationEntry entry) {
        Map<String, Object> map = new HashMap<>();
        map.put("entry-timestamp", entry.getTimestampAsString());
        map.put("entry-number", entry.getEntryNumber());
        List<String> hashes = entry.getItems().stream().map(i -> hashValue(i.getFields())).collect(toList());
        map.put("item-hashes", hashes);
        return map;
    }

    private static String hashValue(Map<String, Object> fields) {
        try {
            return "sha-256:" + DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Map<String, Object>> derivationEntriesAsArray(PartialEntity<DerivationEntry> entity) {
        return entity.getEntries().stream().map(DataViews::derivationEntryAsMap).collect(toList());
    }
}
