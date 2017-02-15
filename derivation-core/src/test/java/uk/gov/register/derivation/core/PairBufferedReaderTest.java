package uk.gov.register.derivation.core;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PairBufferedReaderTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void readPairs() throws Exception {
        InputStream inputStream = Files.newInputStream(Paths.get("src/test/resources", "countries.rsf"));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        PairBufferedReader pairBufferedReader = new PairBufferedReader(bufferedReader);
        List<String> strings = pairBufferedReader.readPairs().collect(Collectors.toList());
        System.out.println(strings);
        assertThat(strings.size(), is(4));
    }

}
