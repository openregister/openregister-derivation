package uk.gov.register.derivation.localauthoritybytype;

public class LocalAuthorityByTypeTransformer extends GroupingTransformer {


    private static final String LOCAL_AUTHORITIES = "local-authorities";
    private static final String LOCAL_AUTHORITY_TYPE = "local-authority-type";

    @Override
    String groupingField() {
        return LOCAL_AUTHORITY_TYPE;
    }

    @Override
    String keyListField() {
        return LOCAL_AUTHORITIES;
    }
}
