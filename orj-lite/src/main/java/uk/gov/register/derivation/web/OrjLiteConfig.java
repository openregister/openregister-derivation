package uk.gov.register.derivation.web;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;

public class OrjLiteConfig extends Configuration {
    @NotEmpty
    private String foo;

    @JsonProperty
    public String getFoo() {
        return foo;
    }

    @JsonProperty
    public void setFoo(String foo) {
        this.foo = foo;
    }
}
