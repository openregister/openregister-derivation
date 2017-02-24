package uk.gov.register.derivation.generic;

import com.google.inject.Inject;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.generic.filters.Filter;
import uk.gov.register.derivation.generic.transformers.Transformer;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class GenericTransformer implements RegisterTransformer {
    private static final String COUNTRIES = "countries";

    private final Set<Filter> filters;
    private final Set<Transformer> transformers;

    @Inject
    public GenericTransformer(Set<Filter> filters, Set<Transformer> transformers) {
        this.filters = filters;
        this.transformers = transformers;
    }

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        // Apply function to remove countries with end-date
        Set<PartialEntity> filteredEntities = newPartialEntities;
        for (Filter filter : filters) {
            filteredEntities = filterEntities(filteredEntities, state, filter);
        }

        // Apply function to group countries by code
        Set<PartialEntity> transformedEntities = filteredEntities;
        for (Transformer transformer : transformers) {
            transformedEntities = groupEntities(filteredEntities, state, transformer);
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

    private Set<PartialEntity> groupEntities(Set<PartialEntity> updates, Set<PartialEntity> state, Transformer transformer) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, identity()));

        Map<Integer, Entry> entries = updates.stream().flatMap(e -> e.getEntries().stream()).collect(Collectors.toMap(e -> e.getEntryNumber(), e -> e));
        Map<String, String> allItems = new HashMap<>();
        state.stream().forEach(pe -> {
            Entry record = pe.getRecord().get();
            List<String> itemsInList = (List<String>) record.getItem().getFields().get(COUNTRIES);

            itemsInList.forEach(key -> allItems.put(key, pe.getKey()));
        });

        transformer.group(entries.values(), currentMaxEntryNumber(state), allItems, stateMap);

        return new HashSet<>(stateMap.values());
    }

    private int currentMaxEntryNumber(Set<PartialEntity> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }
}
