package uk.gov.register.derivation.web.resources;

import uk.gov.register.derivation.web.repo.EntityStore;
import uk.gov.register.derivation.web.util.DataViews;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/record")
@Produces(MediaType.APPLICATION_JSON)
public class RecordResource {
    private final EntityStore entityStore;

    public RecordResource(EntityStore entityStore) {
        this.entityStore = entityStore;
    }

    @GET
    @Path("/{id}")
    public Map<String,Object> read(@PathParam("id") String id) {
        return entityStore.findEntity(id).map(DataViews::recordAsMap).orElseThrow(RuntimeException::new);
    }

    @GET
    @Path("/{id}/entries")
    public List<Map<String,Object>> entries(@PathParam("id") String id) {
        return entityStore.findEntity(id).map(DataViews::entriesAsArray).orElseThrow(RuntimeException::new);
    }

}
