package uk.gov.register.derivation.currentcountriesbycode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;

import java.io.UncheckedIOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class CurrentCountryByCodeTransformer implements RegisterTransformer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String COUNTRIES = "countries";
    private static final String COUNTRY_CODE = "name";
    private static final String COUNTRY_NAME = "name";

    private final DateTimeFormatter dateTimeFormatter;

    public CurrentCountryByCodeTransformer() {
        this.dateTimeFormatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ISO_INSTANT)
                .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM"))
                .toFormatter();
    }

    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        // Apply function to remove countries with end-date
        Set<PartialEntity> countriesWithoutEndDates = removeCountriesWithEndDate(newPartialEntities, state);

        // Apply function to group countries by code
        Set<PartialEntity> countriesGroupedByCode = groupCountriesByCode(countriesWithoutEndDates, state);

        // Return result
        return countriesGroupedByCode;
    }

    private Set<PartialEntity> removeCountriesWithEndDate(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, identity()));

        newPartialEntities.forEach(newEntity -> {
            String countryCode = newEntity.getKey();
            if (endsBefore(newEntity, now())) {
                stateMap.remove(countryCode);
            }
            else if (stateMap.containsKey(countryCode)) {
                stateMap.get(countryCode).getEntries().addAll(newEntity.getEntries());
            }
            else {
                stateMap.put(countryCode, newEntity);
            }
        });

        return new HashSet<>(stateMap.values());
    }

    private Set<PartialEntity> groupCountriesByCode(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state) {
        final Map<String, PartialEntity> stateMap = state.stream().collect(toMap(PartialEntity::getKey, identity()));

        int currentMaxEntryNumber = currentMaxEntryNumber(state);
        AtomicInteger rollingNumber = new AtomicInteger(0);

        Map<Integer, Entry> entries = newPartialEntities.stream().flatMap(e -> e.getEntries().stream()).collect(Collectors.toMap(e -> e.getEntryNumber(), e -> e));
        Map<String, String> allCountries = new HashMap<>();
        state.stream().forEach(pe -> {
            Entry record = pe.getRecord().get();
            List<String> countries = (List<String>) record.getItem().getFields().get(COUNTRIES);

            countries.forEach(la -> allCountries.put(la, pe.getKey()));
        });

        entries.values().forEach(entry -> {
            String countryCode = Character.toString(entry.getItem().getFields().get(COUNTRY_CODE).toString().charAt(0));
            String countryName = (String) entry.getItem().getFields().get(COUNTRY_NAME);

            if (allCountries.containsKey(countryName) && allCountries.get(countryName).equals(countryCode)) {
                // No changes that we care about
                return;
            }

            if (allCountries.containsKey(countryName)) {
                PartialEntity pe = stateMap.get(allCountries.get(countryName));
                List<String> localAuthList = (List<String>) pe.getRecord().orElseThrow(IllegalStateException::new)
                        .getItem().getFields().get(COUNTRIES);

                Entry localAuthoritiesEntryWithRemoved = createCountriesEntryRemoving(countryName, localAuthList, allCountries, pe.getKey(), entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.getAndIncrement());
                pe.getEntries().add(localAuthoritiesEntryWithRemoved);
            }

            if (!stateMap.containsKey(countryCode)) {
                // New LA Type, so create the entry in the Map, but also check that local authority is new
                stateMap.put(countryCode, createCountryCodeEntity(countryCode));
            }

            List<String> currentLocalAuthorities = stateMap.get(countryCode).getRecord().isPresent()
                    ? (List<String>) stateMap.get(countryCode).getRecord().get().getItem().getFields().get(COUNTRIES)
                    : new ArrayList<>();

            if (currentLocalAuthorities == null) {
                currentLocalAuthorities = new ArrayList<>();
            }

            Entry newLocalAuthoritiesEntry = createCountriesEntryAdding(countryName, currentLocalAuthorities, allCountries, countryCode, entry.getEntryNumber() + currentMaxEntryNumber + rollingNumber.get());
            stateMap.get(countryCode).getEntries().add(newLocalAuthoritiesEntry);
        });

        return new HashSet<>(stateMap.values());
    }

    private int currentMaxEntryNumber(Set<PartialEntity> state) {
        return state.stream().mapToInt(pe -> pe.getEntries().size()).sum();
    }

    private Entry createCountriesEntryAdding(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.add(localAuthority);
        allLocalAuthorities.put(localAuthority, localAuthorityType);
        return createCountriesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createCountriesEntryRemoving(String localAuthority, List<String> currentLocalAuthorities, Map<String, String> allLocalAuthorities, String localAuthorityType, int entryNumber) {
        List<String> updatedLocalAuthorities = new ArrayList<>(currentLocalAuthorities);
        updatedLocalAuthorities.remove(localAuthority);
        allLocalAuthorities.remove(localAuthority);
        return createCountriesEntry(updatedLocalAuthorities, localAuthorityType, entryNumber);
    }

    private Entry createCountriesEntry(List<String> newLocalAuthorities, String localAuthorityType, int entryNumber) {
        Map<String, Object> newFields = new HashMap<>();
        newFields.put(COUNTRY_CODE, localAuthorityType);
        newFields.put(COUNTRIES, newLocalAuthorities);
        Item newLocalAuthoritiesItem = new Item(newFields);
        Entry newLocalAuthoritiesEntry = new Entry(entryNumber, Instant.now(), hashValue(newFields));
        newLocalAuthoritiesEntry.setItem(newLocalAuthoritiesItem);
        return newLocalAuthoritiesEntry;
    }

    private PartialEntity createCountryCodeEntity(String countryCode) {
        PartialEntity countryCodeEntity = new PartialEntity(countryCode);
        Map<String, Object> fields = new HashMap<>();
        fields.put(COUNTRY_CODE, countryCode);
        LinkedList<String> localAuthList = new LinkedList<>();
        fields.put(COUNTRIES, localAuthList);
        return countryCodeEntity;
    }

    private String hashValue(Map<String, Object> fields) {
        try {
            return DigestUtils.sha256Hex(OBJECT_MAPPER.writeValueAsString(fields));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
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

    @Override
    public Set<PartialEntity> transform(Set<PartialEntity> newPartialEntities, Set<PartialEntity> state, List<String> filters, List<String> groupers) {
        return null;
    }
}
