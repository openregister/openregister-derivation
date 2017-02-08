package uk.gov.derivation;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

public class RsfExecutor {
    private final ObjectReconstructor objectReconstructor;

    public RsfExecutor() {
        this.objectReconstructor = new ObjectReconstructor();
    }

    public RegisterContent execute(RegisterSerialisationFormat rsf) {
        Iterator<RegisterCommand> commands = rsf.getCommands();
        RegisterContent registerContent = new RegisterContent();

        while (commands.hasNext()) {
            RegisterCommand command = commands.next();
            execute(command, registerContent);
        }

        return registerContent;
    }

    private void execute(RegisterCommand command, RegisterContent registerContent) {
        try {
            if (command.getCommandName().equals("add-item")) {
                String jsonContent = command.getCommandArguments().get(0);
                Item item = new Item(objectReconstructor.reconstruct(jsonContent));
                registerContent.addItem(item);
            } else if (command.getCommandName().equals("append-entry")) {
                List<String> parts = command.getCommandArguments();
                Entry entry = new Entry(HashValue.decode(HashValue.HashingAlgorithm.SHA256, parts.get(1)), Instant.parse(parts.get(0)), parts.get(2));
                registerContent.addEntry(entry);
            }
        }
        catch (Exception e) {

        }
    }
}