package uk.gov.register.derivation;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new Registrar());

        InputStream rsfStream = Files.newInputStream(Paths.get( args[0]));
        RsfParser parser = injector.getInstance(RsfParser.class);
        Set<PartialEntity> entities = parser.parse(rsfStream);

        String jsonResult = JsonSerializer.serialize(entities);
        Uploader uploader = injector.getInstance(Uploader.class);
        uploader.upload("openregister.derivation", "derivation.json", jsonResult);
    }
}