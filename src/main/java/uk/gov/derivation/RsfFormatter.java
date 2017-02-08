package uk.gov.derivation;

import java.util.Arrays;
import java.util.List;

public class RsfFormatter {
    private final String TAB = "\t";

    public RegisterCommand parse(String str) throws SerializedRegisterParseException {
        List<String> parts = Arrays.asList(str.split(TAB));

        if (parts.isEmpty() || parts.size() < 2) {
            throw new SerializedRegisterParseException("String is empty or is in incorrect format");
        }

        String commandName = parts.get(0);
        List<String> commandParameters = parts.subList(1, parts.size());

        return new RegisterCommand(commandName, commandParameters);
    }
}