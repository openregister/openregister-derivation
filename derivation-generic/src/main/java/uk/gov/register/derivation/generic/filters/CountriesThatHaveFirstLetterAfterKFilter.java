package uk.gov.register.derivation.generic.filters;

import uk.gov.register.derivation.core.Entry;
import uk.gov.register.derivation.core.PartialEntity;

import java.util.List;
import java.util.Map;

public class CountriesThatHaveFirstLetterAfterKFilter implements Filter {
    public void apply(PartialEntity entity, Map<String, PartialEntity> stateMap) {
        String countryCode = entity.getKey();
        if (codeStartsAfterK(entity)) {
            stateMap.put(countryCode, entity);
        }
        else {
            stateMap.remove(countryCode);
        }
    }

    public String getName() {
        return "countriesThatStartAfterK";
    }

    private boolean codeStartsAfterK(PartialEntity entity) {
        List<Entry> entries = entity.getEntries();
        Entry current = entries.get(entries.size() - 1);
        String name = (String) current.getItem().getFields().get("country");
        return name.charAt(0) > 'K';
    }

    private Character parseFirstLetter(String name) {
        return name.charAt(0);
    }
}
