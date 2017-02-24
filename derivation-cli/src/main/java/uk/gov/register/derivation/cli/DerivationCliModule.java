package uk.gov.register.derivation.cli;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.generic.GenericGrouper;
import uk.gov.register.derivation.generic.GenericTransformer;
import uk.gov.register.derivation.generic.Grouper;
import uk.gov.register.derivation.generic.filters.CountriesThatHaveFirstLetterAfterKFilter;
import uk.gov.register.derivation.generic.filters.CurrentCountryFilter;
import uk.gov.register.derivation.generic.filters.Filter;
import uk.gov.register.derivation.generic.groupings.CountryByCodeGrouping;
import uk.gov.register.derivation.generic.groupings.CountryByFirstLetterGrouping;
import uk.gov.register.derivation.generic.groupings.Grouping;

public class DerivationCliModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Filter.class).to(CurrentCountryFilter.class);
        bind(RegisterTransformer.class).to(GenericTransformer.class);

        Multibinder<Filter> filters = Multibinder.newSetBinder(binder(), Filter.class);
        filters.addBinding().to(CurrentCountryFilter.class);
        filters.addBinding().to(CountriesThatHaveFirstLetterAfterKFilter.class);

        Multibinder<Grouping> groupings = Multibinder.newSetBinder(binder(), Grouping.class);
        groupings.addBinding().to(CountryByFirstLetterGrouping.class);
        groupings.addBinding().to(CountryByCodeGrouping.class);

        bind(Grouper.class).to(GenericGrouper.class);
    }


}
