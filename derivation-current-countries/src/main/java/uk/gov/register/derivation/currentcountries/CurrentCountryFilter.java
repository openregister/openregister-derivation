package uk.gov.register.derivation.currentcountries;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.*;
import static java.util.stream.Collectors.toSet;

public class CurrentCountryFilter implements RegisterTransformer {


    private final DateTimeFormatter dateTimeFormatter;

    public CurrentCountryFilter() {
        dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_INSTANT)
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM"))
                .toFormatter();
    }

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> entities) {
        final Instant now = Instant.now();
        return entities.stream().filter(e -> !endsBefore(e, now)).collect(toSet());
    }

    public Set<PartialEntity> partialTransform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        Map<String, PartialEntity> stateMap = state.stream().collect(Collectors.toMap(PartialEntity::getKey, Function.identity()));
        newPartialEntities.forEach(newEntity -> {
            if (endsBefore(newEntity, Instant.now())) {
                stateMap.remove(newEntity.getKey());
            }
            else if (stateMap.containsKey(newEntity.getKey())) {
                stateMap.get(newEntity.getKey()).merge(newEntity.getEntries());
            }
            else {
                stateMap.put(newEntity.getKey(), newEntity);
            }
        });
        return new HashSet<>(stateMap.values());
    }

    private boolean endsBefore(PartialEntity entity, final Instant time) {
        List<Entry> entries = entity.getEntries();
        Entry current = entries.get(entries.size() - 1);
        Optional<String> endDate = Optional.ofNullable((String) current.getItem().getFields().get("end-date"));
        return endDate.map(this::parseDate).map(i -> i.isBefore(time)).orElse(false);
    }

    private Instant parseDate(String date) {

        TemporalAccessor temporalAccessor = dateTimeFormatter.parse(date);

        if (temporalAccessor.isSupported(INSTANT_SECONDS)) {
            return Instant.from(temporalAccessor);
        } else if (temporalAccessor.isSupported(DAY_OF_MONTH)) {
            return LocalDate.from(temporalAccessor).atStartOfDay().toInstant(UTC);
        } else if (temporalAccessor.isSupported(MONTH_OF_YEAR)) {
            return YearMonth.from(temporalAccessor).atDay(1).atStartOfDay().toInstant(UTC);
        } else {
            throw new DateTimeParseException("Failed to parse date", date, 0);
        }
    }
}
