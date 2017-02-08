package uk.gov.derivation;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.digest.DigestUtils;

public class Item {
    private static final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private final HashValue hashValue;
    private final JsonNode content;

    public Item(JsonNode content) {
        this(itemHash(content), content);
    }

    public Item(HashValue hashValue, JsonNode content) {
        this.hashValue = hashValue;
        this.content = content;
    }

    public static HashValue itemHash(JsonNode content) {
        String hash = DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));

        return new HashValue(HashValue.HashingAlgorithm.SHA256, hash);
    }

    public HashValue getSha256hex() {
        return hashValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (hashValue != null ? !hashValue.equals(item.hashValue) : item.hashValue != null) return false;
        return content != null ? content.equals(item.content) : item.content == null;

    }

    @Override
    public int hashCode() {
        int result = hashValue != null ? hashValue.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Item{" +
                "content=" + content +
                ", hashValue=" + hashValue +
                '}';
    }
}