package uk.gov.register.derivation.cli;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.*;
import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RsfCreator;
import uk.gov.register.derivation.core.RsfParser;
import uk.gov.register.derivation.generic.GenericTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
        if (args.length > 0) {
            Injector injector = Guice.createInjector(new DerivationCliModule());

            CommandLine commandLine = getCommandLine(args);

            String updateFile = commandLine.getOptionValue("updates");

            RsfParser parser = injector.getInstance(RsfParser.class);
            GenericTransformer transformer = injector.getInstance(GenericTransformer.class);
            RsfCreator rsfCreator = injector.getInstance(RsfCreator.class);

            InputStream updateStream = Files.newInputStream(Paths.get(updateFile));

            Set<PartialEntity> updateEntities = parser.parse(updateStream);

            Set<PartialEntity> stateEntities = Collections.emptySet();

            if (commandLine.hasOption("state")) {
                InputStream stateStream = Files.newInputStream(Paths.get(commandLine.getOptionValue("state")));
                stateEntities = parser.parse(stateStream);
            }

            List<String> filters = new ArrayList<>();
            if (commandLine.hasOption("filters")) {
                filters = Arrays.asList(commandLine.getOptionValue("filters").split(","));
            }

            List<String> groupings = new ArrayList<>();
            if (commandLine.hasOption("groupings")) {
                groupings = Arrays.asList(commandLine.getOptionValue("groupings").split(","));
            }

            Set<PartialEntity> transformed = transformer.transform(updateEntities, stateEntities, filters, groupings);

            String rsf = rsfCreator.serializeAsRsf(transformed);

            System.out.println(rsf);

        } else {
            System.err.println("Usage: args required - [rsf file path updates] [rsf file path current state - optional] ");
        }
    }

    private static CommandLine getCommandLine(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("u", "updates", true, "RSF file containing updates");
        options.addOption("s", "state", true, "RSF state file");

        Option filtersOpt = new Option("filters", true, "Specifies the filters to apply\nOptions:\n- currentCountries");
        filtersOpt.setArgs(1);

        Option groupersOpt = new Option("groupings", true,
                "Specifies the groupings to apply\nOptions:\n- countriesByCode\n- countriesByFirstThreeLetters");
        groupersOpt.setArgs(1);

        options.addOption(filtersOpt);
        options.addOption(groupersOpt);

        CommandLineParser parser = new DefaultParser();

        return parser.parse(options, args);
    }
}
