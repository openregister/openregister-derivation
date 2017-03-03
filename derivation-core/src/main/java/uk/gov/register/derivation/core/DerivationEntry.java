package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class DerivationEntry extends AbstractEntry {
    private String itemHash;

    private Item item;

    public DerivationEntry(int entryNumber, Instant timestamp, String itemHash) {
        super(timestamp, entryNumber);
        this.itemHash = itemHash;
    }

    // used by Jackson
    public DerivationEntry(@JsonProperty("entryNumber") int entryNumber, @JsonProperty("timestamp") String timestamp,
                           @JsonProperty("itemHash") String itemHash) {
        super(timestamp, entryNumber);
        this.itemHash = itemHash;
    }

    public String getItemHash() {
        return itemHash;
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
                "timestamp=" + getTimestamp() +
                ", itemHash='" + itemHash + '\'' +
                ", entryNumber=" + getEntryNumber() +
                ", item=" + item +
                '}';
    }
}
