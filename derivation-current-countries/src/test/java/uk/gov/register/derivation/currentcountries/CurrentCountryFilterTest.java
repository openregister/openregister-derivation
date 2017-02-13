package uk.gov.register.derivation.currentcountries;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CurrentCountryFilterTest {

    private static Instant OCT_1990 = Instant.parse("1990-10-02T00:01:00.00Z");
    private static Instant DEC_1991 = Instant.parse("1991-12-25T00:01:00.00Z");

    private Set<PartialEntity> entities;
    Map<String, Object> germanOldFields;
    Map<String, Object> sovietFields;

    CurrentCountryFilter filter;

    @Before
    public void setup() {
        PartialEntity deEntity = new PartialEntity("DE");
        Map<String, Object> germanFields = new HashMap<>();
        germanFields.put("name", "Germany");

        germanOldFields = new HashMap<>();
        germanOldFields.put("name", "West Germany");

        Entry germanEntry = new Entry(2, OCT_1990, "sha-256:1");
        germanEntry.setItem(new Item(germanFields));

        Entry germanOldEntry = new Entry(1, OCT_1990, "sha-256:2");
        germanOldEntry.setItem(new Item(germanOldFields));

        deEntity.getEntries().add(germanOldEntry);
        deEntity.getEntries().add(germanEntry);

        PartialEntity ussrEntity = new PartialEntity("USSR");
        sovietFields = new HashMap<>();
        sovietFields.put("name", "Union of Soviet Socialist Republics");

        Entry ussrEntry = new Entry(3, DEC_1991, "sha-256:3");
        ussrEntry.setItem(new Item(sovietFields));

        ussrEntity.getEntries().add(ussrEntry);

        entities = new HashSet<>();
        entities.add(deEntity);
        entities.add(ussrEntity);

        filter = new CurrentCountryFilter();
    }

    @Test
    public void shouldFilterCurrentCountries() {
        germanOldFields.put("end-date", "1990-10-02T00:01:00.00Z");
        sovietFields.put("end-date", "1991-12-25T00:01:00.00Z");

        Set<PartialEntity> transformedEntities = filter.transform(entities);

        assertThat(transformedEntities.size(), is(1));
        PartialEntity foundEntity = transformedEntities.iterator().next();
        assertThat(foundEntity.getKey(), is("DE"));
        assertThat(foundEntity.getEntries().size(), is(2));
    }

    @Test
    public void shouldFilterCurrentCountriesWithTruncatedDate() {
        germanOldFields.put("end-date", "1990-10-02");
        sovietFields.put("end-date", "1991-12-25");

        Set<PartialEntity> transformedEntities = filter.transform(entities);

        assertThat(transformedEntities.size(), is(1));
        PartialEntity foundEntity = transformedEntities.iterator().next();
        assertThat(foundEntity.getKey(), is("DE"));
        assertThat(foundEntity.getEntries().size(), is(2));
    }

    @Test
    public void shouldFilterCurrentCountriesWithYearMonthDate() {
        germanOldFields.put("end-date", "1990-10");
        sovietFields.put("end-date", "1991-12");

        Set<PartialEntity> transformedEntities = filter.transform(entities);

        assertThat(transformedEntities.size(), is(1));
        PartialEntity foundEntity = transformedEntities.iterator().next();
        assertThat(foundEntity.getKey(), is("DE"));
        assertThat(foundEntity.getEntries().size(), is(2));
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldNotParseInvalidDate() {
        germanOldFields.put("end-date", "1990.1.3");
        sovietFields.put("end-date", "1991.3.abc");

        filter.transform(entities);
    }
}