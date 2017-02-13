package uk.gov.register.derivation.core;

import java.util.LinkedList;
import java.util.List;

public class PartialEntity {

    private final String key;

    private final List<Entry> entries;

    public PartialEntity(String key) {
        this.key = key;
        this.entries = new LinkedList<>();
    }

    public String getKey() {
        return key;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return "PartialEntity{" +
                "key='" + key + '\'' +
                ", entries=" + entries +
                '}';
    }

    public void merge(List<Entry> newEntries) {
        entries.addAll(newEntries);
    }
}
