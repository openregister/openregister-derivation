package uk.gov.register.derivation.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.RsfParser;
import uk.gov.register.derivation.currentcountries.CurrentCountryFilter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
            Injector injector = Guice.createInjector(new DerivationCliModule());

            InputStream rsfStream = Files.newInputStream(Paths.get(args[0]));
            RsfParser parser = injector.getInstance(RsfParser.class);
            Entry entry = parser.parse(rsfStream);

            CurrentCountryFilter transformer = injector.getInstance(CurrentCountryFilter.class);
            Optional<Entry> transformed = transformer.transform(entry);

            if (transformed.isPresent()) {
                String jsonResult = JsonSerializer.serialize(transformed);
                Uploader uploader = injector.getInstance(Uploader.class);
                uploader.upload(args[1], args[2], jsonResult);
            }
        } else {
            System.err.println("Usage: args required - [rsf file path] [s3 bucket] [s3 key]");
        }
    }
}