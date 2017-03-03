package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DerivationEntry extends AbstractEntry {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Set<Item> items;

    public DerivationEntry(int entryNumber, Instant timestamp) {
        super(timestamp, entryNumber);
        this.items = new HashSet<>();
    }

    // used by Jackson
    public DerivationEntry(@JsonProperty("entryNumber") int entryNumber, @JsonProperty("timestamp") String timestamp) {
        super(timestamp, entryNumber);
    }

    public Set<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "timestamp=" + getTimestamp() +
                ", entryNumber=" + getEntryNumber() +
                ", item=" + items +
                '}';
    }

}
