package uk.gov.register.derivation.generic.groupings;

import uk.gov.register.derivation.core.Item;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CountryByFirstLetterGrouping implements Grouping {
    @Override
    public String getKeyFieldName() {
        return "name";
    }

    @Override
    public String getItemFieldName() {
        return "countries";
    }

    @Override
    public String getItemField() {
        return "country";
    }

    @Override
    public String getName() {
        return "countriesByFirstThreeLetters";
    }

    @Override
    public Optional<String> transformKey(String key) {
        return Optional.of(key.substring(0, 3));
    }

    @Override
    public List<String> calculateKeys(Item item) {
        return Arrays.asList(item.getFields().get(getKeyFieldName()).toString().substring(0, 3));
    }
}
