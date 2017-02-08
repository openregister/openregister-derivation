package uk.gov.derivation;

import java.util.Iterator;

public class RegisterSerialisationFormat {
    private Iterator<RegisterCommand> commands;

    public RegisterSerialisationFormat(Iterator<RegisterCommand> commands) {
        this.commands = commands;
    }

    public Iterator<RegisterCommand> getCommands() {
        return commands;
    }
}
