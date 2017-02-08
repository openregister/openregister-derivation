package uk.gov.derivation;

public class HashValue {
    private final String value;
    private final String hashingAlgorithm;

    public HashValue(HashingAlgorithm hashingAlgorithm, String value) {
        this.hashingAlgorithm = hashingAlgorithm.toString();
        this.value = value;
    }

    public HashValue(String hashingAlgorithm, String value) {
        this.hashingAlgorithm = hashingAlgorithm;
        this.value = value;
    }

    public static HashValue decode(HashingAlgorithm hashingAlgorithm, String encodedHash) {
        if (!encodedHash.startsWith(hashingAlgorithm.toString())) {
            throw new RuntimeException(String.format("Hash \"%s\" has not been encoded with hashing algorithm \"%s\"", encodedHash, hashingAlgorithm));
        }

        String[] parts = encodedHash.split(hashingAlgorithm + ":");

        if (parts.length != 2) {
            throw new RuntimeException(String.format("Cannot decode hash %s", encodedHash));
        }

        return new HashValue(hashingAlgorithm, parts[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != this.getClass()) return false;

        HashValue hashValue = (HashValue) o;

        if (value != null ? !value.equals(hashValue.value) : hashValue.value != null) return false;

        return hashingAlgorithm != null ? hashingAlgorithm.equals(hashValue.hashingAlgorithm) : hashValue.hashingAlgorithm == null;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (hashingAlgorithm != null ? hashingAlgorithm.hashCode() : 0);
        return result;
    }

    public enum HashingAlgorithm {
        SHA256 {
            @Override
            public String toString() {
                return "sha-256";
            }
        }
    }
}