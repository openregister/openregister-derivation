package uk.gov.register.derivation;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;

public class Registrar extends AbstractModule {
    @Override
    protected void configure() {
        bind(AmazonS3.class).to(AmazonS3Client.class);
    }
}
