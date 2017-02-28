package uk.gov.register.derivation.generic.groupings;

import uk.gov.register.derivation.core.Item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SchoolsByAgeGrouping implements Grouping {
    @Override
    public String getKeyFieldName() {
        return "pupil-age";
    }

    @Override
    public String getItemFieldName() {
        return "school-eng";
    }

    @Override
    public String getItemField() {
        return "school-eng";
    }

    @Override
    public String getName() {
        return "schoolsByAge";
    }

    @Override
    public Optional<String> transformKey(String key) {
        return null;
    }

    @Override
    public List<String> calculateKeys(Item item) {
        int minAge = Integer.parseInt(item.getFields().get("minimum-age").toString());
        int maxAge = Integer.parseInt(item.getFields().get("maximum-age").toString());

        return IntStream.rangeClosed(minAge, maxAge).mapToObj(i -> Integer.toString(i)).collect(Collectors.toList());
    }
}
