package uk.gov.register.derivation.core;

import com.amazonaws.services.s3.AmazonS3;
import org.junit.Test;
import uk.gov.register.derivation.cli.Uploader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UploaderTest {
    @Test
    public void shouldUploadJsonFileToS3() {
        String bucketName = "openregister.derivation";
        String key = "derivation.json";
        String content = "{\n" +
                "  \"key\": \"CZ\",\n" +
                "  \"entries\": [\n" +
                "    {\n" +
                "      \"itemHash\": \"sha-256:c45bd0b4785680534e07c627a5eea0d2f065f0a4184a02ba2c1e643672c3f2ed\",\n" +
                "      \"sequenceNumber\": 1,\n" +
                "      \"item\": {\n" +
                "        \"fields\": {\n" +
                "          \"country\": \"CZ\",\n" +
                "          \"official-name\": \"The Czech Republic\",\n" +
                "          \"name\": \"Czech Republic\",\n" +
                "          \"start-date\": \"1993-01-01\",\n" +
                "          \"citizen-names\": \"Czech\"\n" +
                "        }\n" +
                "      },\n" +
                "      \"timestamp\": \"2016-11-11T16:25:07Z\"\n" +
                "    }" +
                "  ]\n" +
                "}";

        AmazonS3 client = mock(AmazonS3.class);

        Uploader uploader = new Uploader(client);
        uploader.upload(bucketName, key, content);

        verify(client).putObject(bucketName, key, content);
    }
}
