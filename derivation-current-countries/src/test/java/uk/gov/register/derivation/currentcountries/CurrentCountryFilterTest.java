package uk.gov.register.derivation.currentcountries;

import org.junit.Before;
import org.junit.Test;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.Item;
import uk.gov.register.derivation.core.PartialEntity;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CurrentCountryFilterTest {

    private static Instant DEC_1991 = Instant.parse("1991-12-25T00:01:00.00Z");

    CurrentCountryFilter filter;

    @Before
    public void setup() {
        filter = new CurrentCountryFilter();
    }

    @Test
    public void shouldFilterCurrentCountries() {
        Set<PartialEntity> entities = new HashSet<>();
        entities.add(partialEntity("DE", asList("1990-10-02T00:01:00.00Z", "")));
        entities.add(partialEntity("SU", asList("1991-12-25T00:01:00.00Z")));

        Set<PartialEntity> transformedEntities = filter.transform(entities, emptySet());

        assertThat(transformedEntities.size(), is(1));
        PartialEntity foundEntity = transformedEntities.iterator().next();
        assertThat(foundEntity.getKey(), is("DE"));
        assertThat(foundEntity.getEntries().size(), is(2));
    }

    @Test
    public void shouldFilterCurrentCountriesWithTruncatedDate() {
        Set<PartialEntity> entities = new HashSet<>();
        entities.add(partialEntity("DE", asList("1990-10-02", "")));
        entities.add(partialEntity("SU", asList("1991-12-25")));

        Set<PartialEntity> transformedEntities = filter.transform(entities, emptySet());

        assertThat(transformedEntities.size(), is(1));
        PartialEntity foundEntity = transformedEntities.iterator().next();
        assertThat(foundEntity.getKey(), is("DE"));
        assertThat(foundEntity.getEntries().size(), is(2));
    }

    @Test
    public void shouldFilterCurrentCountriesWithYearMonthDate() {
        Set<PartialEntity> entities = new HashSet<>();
        entities.add(partialEntity("DE", asList("1990-10", "")));
        entities.add(partialEntity("SU", asList("1991-12")));
        Set<PartialEntity> transformedEntities = filter.transform(entities, emptySet());

        assertThat(transformedEntities.size(), is(1));
        PartialEntity foundEntity = transformedEntities.iterator().next();
        assertThat(foundEntity.getKey(), is("DE"));
        assertThat(foundEntity.getEntries().size(), is(2));
    }

    @Test
    public void shouldExpireCountry() {
        PartialEntity suCreate = partialEntity("SU", asList(""));
        Set<PartialEntity> transformedEntities = filter.transform(singleton(suCreate), new HashSet<>());
        assertThat(transformedEntities.size(), is(1));

        PartialEntity suExpire = partialEntity("SU", asList("1991-12"));
        Set<PartialEntity> transformedEntities2 = filter.transform(singleton(suExpire), transformedEntities);

        assertThat(transformedEntities2.size(), is(0));
    }

    @Test
    public void shouldAddFurtherEntriesToCountry() {
        PartialEntity deCreate = partialEntity("DE", asList(""));
        Set<PartialEntity> transformedEntities = filter.transform(singleton(deCreate), new HashSet<>());
        assertThat(transformedEntities.size(), is(1));
        assertThat(transformedEntities.iterator().next().getEntries().size(), is(1));

        PartialEntity deUpdate = partialEntity("DE", asList(""));
        Set<PartialEntity> transformedEntities2 = filter.transform(singleton(deUpdate), transformedEntities);

        assertThat(transformedEntities2.size(), is(1));
        assertThat(transformedEntities2.iterator().next().getEntries().size(), is(2));
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldNotParseInvalidDate() {
        Set<PartialEntity> entities = new HashSet<>();
        entities.add(partialEntity("DE", asList("1990.1.1")));
        filter.transform(entities, emptySet());
    }

    private PartialEntity partialEntity(String key, List<String> endDates) {
        PartialEntity entity = new PartialEntity(key);

        endDates.forEach(date -> {
            Entry entry = new Entry(1, DEC_1991, "sha-256:1");
            Item item = new Item(new HashMap<>());
            item.getFields().put("name", key);
            if (!date.isEmpty()) {
                item.getFields().put("end-date", date);
            }
            entry.setItem(item);
            entity.getEntries().add(entry);
        });

        return entity;
    }
}
