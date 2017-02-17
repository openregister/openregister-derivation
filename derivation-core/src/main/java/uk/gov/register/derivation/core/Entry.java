package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class Entry {

    private Instant timestamp;

    private String itemHash;

    private int entryNumber;

    private Item item;

    public Entry(int entryNumber, Instant timestamp, String itemHash) {
        this.timestamp = timestamp;
        this.itemHash = itemHash;
        this.entryNumber = entryNumber;
    }

    // used by Jackson
    public Entry(@JsonProperty("entryNumber") int entryNumber, @JsonProperty("timestamp") String timestamp,
                 @JsonProperty("itemHash") String itemHash) {
        this.timestamp = Instant.parse(timestamp);
        this.itemHash = itemHash;
        this.entryNumber = entryNumber;
    }

    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public String getTimestampAsString() {
        return ISO_INSTANT.format(timestamp.truncatedTo(ChronoUnit.SECONDS));
    }

    public String getItemHash() {
        return itemHash;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "timestamp=" + timestamp +
                ", itemHash='" + itemHash + '\'' +
                ", entryNumber=" + entryNumber +
                ", item=" + item +
                '}';
    }
}
