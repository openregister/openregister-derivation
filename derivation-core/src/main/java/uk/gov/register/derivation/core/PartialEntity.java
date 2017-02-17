package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class PartialEntity {

    private final String key;

    private final List<Entry> entries;

    public PartialEntity(@JsonProperty("key") String key) {
        this.key = key;
        this.entries = new LinkedList<>();
    }

    public String getKey() {
        return key;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @JsonIgnore
    public Entry getRecord() {
        return entries.get(entries.size() - 1);
    }

    @Override
    public String toString() {
        return "PartialEntity{" +
                "key='" + key + '\'' +
                ", entries=" + entries +
                '}';
    }

}
