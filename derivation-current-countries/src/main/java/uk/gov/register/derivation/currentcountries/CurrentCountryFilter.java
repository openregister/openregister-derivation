package uk.gov.register.derivation.currentcountries;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.RegisterTransformer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.*;

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
    public Optional<Entry> transform(Entry entry) {
        final Instant now = Instant.now();
        return endsBefore(entry, now) ? Optional.of(entry) : Optional.empty();
    }

    private boolean endsBefore(Entry entry, final Instant time) {
        Optional<String> endDate = Optional.ofNullable((String) entry.getItem().getFields().get("end-date"));
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
