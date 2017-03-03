package uk.gov.register.derivation.web.util;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DataViews {

    public static Map<String, Map<String, Object>> recordsAsMap(Collection<PartialEntity> entities) {
        return entities.stream().collect(toMap(PartialEntity::getKey, DataViews::recordAsMap));
    }

    public static Map<String, Object> recordAsMap(PartialEntity<Entry> entity) {
        Map<String, Object> combinedItemEntryMap = new HashMap<>();
        Entry record = entity.getRecord().orElseThrow(IllegalStateException::new);
        combinedItemEntryMap.putAll(record.getItem().getFields());
        combinedItemEntryMap.putAll( entryAsMap( record ));
        return combinedItemEntryMap;
    }

    private static Map<String,Object> entryAsMap(Entry entry){
        Map<String, Object> map = new HashMap<>();
        map.put("entry-timestamp", entry.getTimestampAsString());
        map.put("entry-number", entry.getEntryNumber());
        map.put("item-hash", entry.getItemHash());
        return map;
    }

    public static List<Map<String,Object>> entriesAsArray(PartialEntity<Entry> partialEntity) {
        return partialEntity.getEntries().stream().map(DataViews::entryAsMap).collect(toList());
    }
}
