package uk.gov.register.derivation.generic.groupings;

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
}
