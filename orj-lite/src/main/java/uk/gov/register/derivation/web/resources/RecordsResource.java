package uk.gov.register.derivation.web.resources;

import uk.gov.register.derivation.web.repo.EntityStore;
import uk.gov.register.derivation.web.service.UpdateService;
import uk.gov.register.derivation.web.util.DataViews;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RecordsResource {

    private final EntityStore entityStore;
    private final UpdateService updateService;

    public RecordsResource(EntityStore entityStore, UpdateService updateService) {
        this.entityStore = entityStore;
        this.updateService = updateService;
    }

    @GET
    @Path("/records")
    public Map<String,Map<String,Object>> read() {
        return DataViews.recordsAsMap(entityStore.allEntities());
    }

    @POST
    @PermitAll
    @Consumes("application/uk-gov-rsf")
    @Path("/load-rsf")
    public Response loadRsf(PartialEntitySetWrapper updateEntities) {
        updateService.update(updateEntities.entitySet);
        return Response.accepted().build();
    }
}

