package uk.gov.register.derivation.web.resources;

import uk.gov.register.derivation.core.PartialEntity;
import uk.gov.register.derivation.core.RsfParser;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

@Provider
@Consumes("application/uk-gov-rsf")
public class RegisterMessageBodyReader implements MessageBodyReader<PartialEntitySetWrapper> {

    @Override
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public PartialEntitySetWrapper readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        RsfParser parser = new RsfParser();
        Set<PartialEntity> entitySet = parser.parse(entityStream);
        return new PartialEntitySetWrapper(entitySet);
    }
}
