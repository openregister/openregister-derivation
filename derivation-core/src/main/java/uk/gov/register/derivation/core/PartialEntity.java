package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
    public Optional<Entry> getRecord() {
        if (entries.size() > 0) {
            return Optional.of(entries.get(entries.size() - 1));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "PartialEntity{" +
                "key='" + key + '\'' +
                ", entries=" + entries +
                '}';
    }

}
