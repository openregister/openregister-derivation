package uk.gov.register.derivation.core;

import java.util.Map;

public class Item {

    private final Map<String,Object> fields;

    public Item(Map<String,Object> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
