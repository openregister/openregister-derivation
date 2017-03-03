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

@Path("/by-type")
@Produces(MediaType.APPLICATION_JSON)
public class DerivationResource {

    private final EntityStore<DerivationEntry> entityStore;

    public DerivationResource(EntityStore<DerivationEntry> entityStore) {
        this.entityStore = entityStore;
    }

    @GET
    @Path("/records")
    public Map<String, Map<String, Object>> all() {
        return DataViews.derivationRecordsAsMap(entityStore.allEntities());
    }


    @GET
    @Path("/record/{id}")
    public Map<String, Object> read(@PathParam("id") String id) {
        return entityStore.findEntity(id).map(DataViews::derivationRecordAsMap).orElseThrow(RuntimeException::new);
    }

    @GET
    @Path("/record/{id}/entries")
    public List<Map<String, Object>> entries(@PathParam("id") String id) {
        return entityStore.findEntity(id).map(DataViews::derivationEntriesAsArray).orElseThrow(RuntimeException::new);
    }


}
