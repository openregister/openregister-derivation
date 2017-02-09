package uk.gov.register.derivation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream rsfStream = Files.newInputStream(Paths.get( args[0]));
        RsfParser parser = new RsfParser();
        Set<PartialEntity> entities = parser.parse(rsfStream);
        Map<String, PartialEntity> partialEntityMap = entities.stream().collect(Collectors.toMap(PartialEntity::getKey, Function.identity()));
        PartialEntity czechia = partialEntityMap.get("CZ");
        System.out.println(czechia);
    }
}