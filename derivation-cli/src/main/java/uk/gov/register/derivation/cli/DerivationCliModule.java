package uk.gov.register.derivation.cli;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.generic.GenericTransformer;
import uk.gov.register.derivation.generic.filters.CurrentCountryFilter;
import uk.gov.register.derivation.generic.filters.Filter;
import uk.gov.register.derivation.generic.groupers.GenericGrouper;
import uk.gov.register.derivation.generic.groupers.Grouper;

public class DerivationCliModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Filter.class).to(CurrentCountryFilter.class);
        bind(RegisterTransformer.class).to(GenericTransformer.class);

        Multibinder<Filter> filters = Multibinder.newSetBinder(binder(), Filter.class);
        filters.addBinding().to(CurrentCountryFilter.class);

        Multibinder<Grouper> transformers = Multibinder.newSetBinder(binder(), Grouper.class);
        transformers.addBinding().to(GenericGrouper.class);
    }
}
