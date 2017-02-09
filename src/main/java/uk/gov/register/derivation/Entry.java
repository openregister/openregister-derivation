package uk.gov.register.derivation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public class Entry {

    private Instant timestamp;

    private String itemHash;

    private int sequenceNumber;

    private Item item;

    public Entry(int sequenceNumber, Instant timestamp, String itemHash) {
        this.timestamp = timestamp;
        this.itemHash = itemHash;
        this.sequenceNumber = sequenceNumber;
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

    public int getSequenceNumber() {
        return sequenceNumber;
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
                ", sequenceNumber=" + sequenceNumber +
                ", item=" + item +
                '}';
    }
}
