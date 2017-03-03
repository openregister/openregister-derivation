package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

public abstract class AbstractEntry {

    private Instant timestamp;
    private int entryNumber;

    public AbstractEntry(Instant timestamp, int entryNumber) {
        this.timestamp = timestamp;
        this.entryNumber = entryNumber;
    }

    public AbstractEntry(String timestamp, int entryNumber) {
        this.timestamp = Instant.parse(timestamp);
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

    public int getEntryNumber() {
        return entryNumber;
    }
}
