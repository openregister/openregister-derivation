package uk.gov.register.derivation.cli;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.generic.GenericTransformer;
import uk.gov.register.derivation.generic.filters.CurrentCountryFilter;
import uk.gov.register.derivation.generic.filters.Filter;
import uk.gov.register.derivation.generic.groupers.CountryByFirstLetterGrouping;
import uk.gov.register.derivation.generic.groupers.GenericGrouper;
import uk.gov.register.derivation.generic.groupers.Grouper;
import uk.gov.register.derivation.generic.groupers.Grouping;

public class DerivationCliModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Filter.class).to(CurrentCountryFilter.class);
        bind(RegisterTransformer.class).to(GenericTransformer.class);

        Multibinder<Filter> filters = Multibinder.newSetBinder(binder(), Filter.class);
        filters.addBinding().to(CurrentCountryFilter.class);

        Multibinder<Grouping> groupings = Multibinder.newSetBinder(binder(), Grouping.class);
        groupings.addBinding().to(CountryByFirstLetterGrouping.class);

        bind(Grouper.class).to(GenericGrouper.class);
    }
}
