package uk.gov.register.derivation.web.resources;

import uk.gov.register.derivation.core.DerivationEntry;
import uk.gov.register.derivation.web.repo.EntityStore;
import uk.gov.register.derivation.web.util.DataViews;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/derivation/{derivation-name}")
@Produces(MediaType.APPLICATION_JSON)
public class DerivationResource {

    private final Map<String,EntityStore<DerivationEntry>> entityStores;

    public DerivationResource(Map<String,EntityStore<DerivationEntry>> entityStores) {
        this.entityStores = entityStores;
    }

    @GET
    @Path("/records")
    public Map<String, Map<String, Object>> all(@PathParam("derivation-name") String derivationName) {
        validateDerivationName(derivationName);
        EntityStore<DerivationEntry> entityStore = entityStores.get(derivationName);
        return DataViews.derivationRecordsAsMap(entityStore.allEntities());
    }

    @GET
    @Path("/record/{id}")
    public Map<String, Object> read(@PathParam("derivation-name") String derivationName, @PathParam("id") String id) {
        validateDerivationName(derivationName);
        EntityStore<DerivationEntry> entityStore = entityStores.get(derivationName);
        return entityStore.findEntity(id).map(DataViews::derivationRecordAsMap).orElseThrow(RuntimeException::new);
    }

    @GET
    @Path("/record/{id}/entries")
    public List<Map<String, Object>> entries(@PathParam("derivation-name") String derivationName, @PathParam("id") String id) {
        validateDerivationName(derivationName);
        EntityStore<DerivationEntry> entityStore = entityStores.get(derivationName);
        return entityStore.findEntity(id).map(DataViews::derivationEntriesAsArray).orElseThrow(RuntimeException::new);
    }

    private void validateDerivationName(String derivationName) {
        if ( !entityStores.containsKey( derivationName)){
            throw new RuntimeException("derivation name not found");
        }
    }


}
