package uk.gov.register.derivation.generic;

import com.google.inject.Inject;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.generic.filters.Filter;
import uk.gov.register.derivation.generic.groupings.Grouping;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class GenericTransformer implements RegisterTransformer {
    private static final String COUNTRIES = "countries";

    private final Map<String, Filter> allFilters;
    private final Map<String, Grouping> allGroupings;
    private final Grouper grouper;

    @Inject
    public GenericTransformer(Set<Filter> allFilters, Set<Grouping> allGroupings, Grouper grouper) {
        this.allFilters = allFilters.stream().collect(Collectors.toMap(f -> f.getName(), f -> f));
        this.allGroupings = allGroupings.stream().collect(Collectors.toMap(g -> g.getName(), g -> g));
        this.grouper = grouper;
    }

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state, List<String> filters, List<String> groupings) {
        // Apply function to remove countries with end-date
        Set<PartialEntity> filteredEntities = newPartialEntities;
        for (String filter : filters) {
            if (!allFilters.containsKey(filter)) {
                throw new RuntimeException("Unknown filter specified: " + filter);
            }

            filteredEntities = filterEntities(filteredEntities, state, allFilters.get(filter));
        }

        // Apply function to group countries by code
        Set<PartialEntity> transformedEntities = filteredEntities;
        for (String grouping : groupings) {
            if (!allGroupings.containsKey(grouping)) {
                throw new RuntimeException("Unknown grouping specified: " + grouping);
            }

            transformedEntities = groupEntities(filteredEntities, state, allGroupings.get(grouping));
        }

        // Return result
        return transformedEntities;
    }

    private Set<PartialEntity> filterEntities(Set<PartialEntity> updates, Set<PartialEntity> state, Filter filter) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, identity()));

        updates.forEach(entity -> {
            filter.apply(entity, stateMap);
        });

        return new HashSet<>(stateMap.values());
    }

    private Set<PartialEntity> groupEntities(Set<PartialEntity> updates, Set<PartialEntity> state, Grouping grouping) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, identity()));

        Map<Integer, Entry> entries = updates.stream().flatMap(e -> e.getEntries().stream()).collect(Collectors.toMap(e -> e.getEntryNumber(), e -> e));
        Map<String, String> allItems = new HashMap<>();
        state.stream().forEach(pe -> {
            Entry record = pe.getRecord().get();
            List<String> itemsInList = (List<String>) record.getItem().getFields().get(COUNTRIES);

            itemsInList.forEach(key -> allItems.put(key, pe.getKey()));
        });

        grouper.group(entries.values(), currentMaxEntryNumber(state), allItems, stateMap, grouping);

        return new HashSet<>(stateMap.values());
    }

    private int currentMaxEntryNumber(Set<PartialEntity> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }
}
