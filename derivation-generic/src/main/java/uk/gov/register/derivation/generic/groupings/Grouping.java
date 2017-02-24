package uk.gov.register.derivation.generic.groupings;

import java.util.Optional;

public interface Grouping {
    String getKeyFieldName();
    String getItemFieldName();
    String getItemField();
    String getName();

    Optional<String> transformKey(String key);
}
