package uk.gov.register.derivation.core;

import java.util.Optional;

public interface RegisterTransformer {

    Optional<Entry> transform(Entry entry);
}
