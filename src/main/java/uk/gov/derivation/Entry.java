package uk.gov.derivation;

import java.time.Instant;

public class Entry {
    private final HashValue hashValue;
    private final Instant timestamp;
    private String key;

    public Entry(HashValue hashValue, Instant timestamp, String key) {
        this.hashValue = hashValue;
        this.timestamp = timestamp;
        this.key = key;
    }

    public HashValue getHashValue() {
        return hashValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        return hashValue == null ? entry.hashValue == null : hashValue.equals(entry.hashValue);
    }

    @Override
    public int hashCode() {
        int result = hashValue != null ? hashValue.hashCode() : 0;
        return result;
    }
}