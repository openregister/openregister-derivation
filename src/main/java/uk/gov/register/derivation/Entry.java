package uk.gov.register.derivation;

import java.time.Instant;

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

    public Instant getTimestamp() {
        return timestamp;
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
