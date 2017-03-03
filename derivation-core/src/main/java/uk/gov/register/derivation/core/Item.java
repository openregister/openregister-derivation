package uk.gov.register.derivation.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Item {

    private final Map<String,Object> fields;

    public Item(@JsonProperty("fields") Map<String,Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "Item{" +
                "fields=" + fields +
                '}';
    }
}
