package uk.gov.register.derivation.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RsfCreator;
import uk.gov.register.derivation.core.RsfParser;
import uk.gov.register.derivation.localauthoritybytype.LocalAuthorityByTypeTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            String updateFile = args[0];

            Injector injector = Guice.createInjector(new DerivationCliModule());

            RsfParser parser = injector.getInstance(RsfParser.class);
            LocalAuthorityByTypeTransformer transformer = injector.getInstance(LocalAuthorityByTypeTransformer.class);
            RsfCreator rsfCreator = injector.getInstance(RsfCreator.class);

            InputStream updateStream = Files.newInputStream(Paths.get(updateFile));

            Set<PartialEntity> updateEntities = parser.parse(updateStream);

            Set<PartialEntity> stateEntities = Collections.emptySet();

            if ( args.length == 2){
                InputStream stateStream = Files.newInputStream(Paths.get(args[1]));
                stateEntities = parser.parse(stateStream);
            }

            Set<PartialEntity> transformed = transformer.transform(updateEntities, stateEntities);

            String rsf = rsfCreator.serializeAsRsf(transformed);

            System.out.println(rsf);

        } else {
            System.err.println("Usage: args required - [rsf file path updates] [rsf file path current state - optional] ");
        }
    }
}
