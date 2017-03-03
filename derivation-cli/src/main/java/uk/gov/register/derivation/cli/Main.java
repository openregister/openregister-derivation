package uk.gov.register.derivation.cli;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RegisterTransformer;
import uk.gov.register.derivation.core.RsfParser;
import uk.gov.register.derivation.localauthoritybytype.LocalAuthorityByTypeTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
            String bucketName = args[1];
            String objectName = args[2];
            Injector injector = Guice.createInjector(new DerivationCliModule());

            InputStream rsfStream = Files.newInputStream(Paths.get(args[0]));
            RsfParser parser = injector.getInstance(RsfParser.class);
            Set<PartialEntity> entities = parser.parse(rsfStream);

            Set<PartialEntity> partialEntities = Collections.emptySet();
            AmazonS3 amazonS3 = injector.getInstance(AmazonS3.class);
            if (amazonS3.doesObjectExist(bucketName, objectName)) {
                InputStream objectContent = amazonS3.getObject(bucketName, objectName).getObjectContent();
                partialEntities = JsonSerializer.deserialize(objectContent, new TypeReference<Set<PartialEntity>>() {});
            }

            RegisterTransformer transformer = injector.getInstance(LocalAuthorityByTypeTransformer.class);
            Collection<PartialEntity> transformed = transformer.transform(entities, partialEntities);

            String jsonResult = JsonSerializer.serialize(transformed);
            amazonS3.putObject(bucketName, objectName, jsonResult);
        } else {
            System.err.println("Usage: args required - [rsf file path] [s3 bucket] [s3 key]");
        }
    }
}
