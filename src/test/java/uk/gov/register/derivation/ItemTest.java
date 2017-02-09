package uk.gov.register.derivation;

import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ItemTest {
    @Test
    public void shouldGetStringFields() throws Exception {
        String itemJson = "{\"citizen-names\":\"Czech\",\"country\":\"CZ\",\"name\":\"Czech Republic\"," +
                "\"official-name\":\"The Czech Republic\",\"start-date\":\"1993-01-01\"}";
        Item item = new Item(itemJson);
        assertTrue(item.getFields().containsKey("name"));
        assertThat(item.getFields().get("name"), is("Czech Republic"));
    }

    @Test
    public void shouldGetIntFields() throws Exception {
        String itemJson = "{\"country\":\"CZ\",\"population\": 33000000 }";
        Item item = new Item(itemJson);
        assertTrue(item.getFields().containsKey("population"));
        assertThat(item.getFields().get("population"), is(33000000));
    }

    @Test
    public void shouldGetArrayFields() throws Exception {
        String itemJson = "{\"country\":\"CZ\",\"centre-lat-long\": [123,345] }";
        Item item = new Item(itemJson);
        assertTrue(item.getFields().containsKey("centre-lat-long"));
        assertThat(item.getFields().get("centre-lat-long"), instanceOf(ArrayList.class));
        ArrayList latLong = (ArrayList) item.getFields().get("centre-lat-long");
        assertThat(latLong.get(0), is(123));
    }

}