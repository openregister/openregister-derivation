package uk.gov.register.derivation.core;

public class KeyedEntry {

    public final String key;
    public final Entry entry;

    public KeyedEntry(Entry entry, String key) {
        this.entry = entry;
        this.key = key;
    }
}


