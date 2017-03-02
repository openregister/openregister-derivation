package uk.gov.register.derivation.web.health;

import com.codahale.metrics.health.HealthCheck;

public class BogusHealthCheck extends HealthCheck {
    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}
