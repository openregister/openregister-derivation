package uk.gov.register.derivation.generic.groupings;

import java.util.Optional;

public class CountryByFirstLetterGrouping implements Grouping {
    private static final String COUNTRIES = "countries";
    private static final String COUNTRY_CODE = "name";
    private static final String COUNTRY_NAME = "name";

    @Override
    public String getKeyFieldName() {
        return COUNTRY_CODE;
    }

    @Override
    public String getItemFieldName() {
        return COUNTRIES;
    }

    @Override
    public String getItemField() {
        return COUNTRY_NAME;
    }

    @Override
    public Optional<String> transformKey(String key) {
        return Optional.of(key.substring(0, 3));
    }
}
