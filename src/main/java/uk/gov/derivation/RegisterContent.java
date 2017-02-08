package uk.gov.derivation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegisterContent {
    private final Set<Item> items;
    private final List<Entry> entries;

    public RegisterContent() {
        this.items = new HashSet<>();
        this.entries = new ArrayList<>();
    }

    public void addItem(Item item) {
        if (!items.contains(item)) {
            items.add(item);
        }
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public Set<Item> getItems() {
        return items;
    }

    public List<Entry> getEntries() {
        return entries;
    }
}
