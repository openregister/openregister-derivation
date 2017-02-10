package uk.gov.register.derivation.cli;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.google.inject.Inject;

public class Uploader {
    private final AmazonS3 client;

    @Inject
    public Uploader(AmazonS3 client) {
        this.client = client;
    }

    public void upload(String bucketName, String key, String value) {
        try {
            client.putObject(bucketName, key, value);
        }
        catch (SdkClientException ex) {
            ex.printStackTrace();
        }
    }
}