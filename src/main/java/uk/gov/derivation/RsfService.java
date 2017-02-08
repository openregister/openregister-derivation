package uk.gov.derivation;

import com.google.inject.Inject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class RsfService {
    private final RsfExecutor rsfExecutor;
    private final RsfFormatter rsfFormatter;

    @Inject
    public RsfService(RsfExecutor rsfExecutor, RsfFormatter rsfFormatter) {
        this.rsfExecutor = rsfExecutor;
        this.rsfFormatter = rsfFormatter;
    }

    public RegisterSerialisationFormat readFrom(String path) {
        try {
            InputStream commandStream = Files.newInputStream(Paths.get(path));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(commandStream));
            Iterator<RegisterCommand> commandsIterator = buffer.lines()
                    .map(rsfFormatter::parse)
                    .iterator();
            return new RegisterSerialisationFormat(commandsIterator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public RegisterContent convert(RegisterSerialisationFormat rsf) {
        return rsfExecutor.execute(rsf);
    }
}
