package uk.gov.derivation;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new Registrar());

        String rsfFilePath = System.getProperty("path");
        if (rsfFilePath == null || rsfFilePath.isEmpty()) {
            System.out.println("No RSF file path provided, terminating.");
            return;
        }

        RsfService rsfService = injector.getInstance(RsfService.class);
        RegisterSerialisationFormat rsf = rsfService.readFrom(rsfFilePath);
        RegisterContent registerContent = rsfService.convert(rsf);

        System.out.println("Ready...");
        System.in.read();
    }
}