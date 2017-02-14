package uk.gov.register.derivation.cli;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.currentcountries.CurrentCountryFilter;

public class DerivationCliModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AmazonS3.class).to(AmazonS3Client.class);
    }
}
