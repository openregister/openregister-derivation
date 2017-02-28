package uk.gov.register.derivation.generic.groupings;

import uk.gov.register.derivation.core.Item;

import java.util.List;
import java.util.Optional;

public interface Grouping {
    String getKeyFieldName();
    String getItemFieldName();
    String getItemField();
    String getName();

    Optional<String> transformKey(String key);

    List<String> calculateKeys(Item item);
}
