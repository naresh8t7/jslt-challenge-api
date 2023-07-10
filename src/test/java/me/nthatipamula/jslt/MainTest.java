
package me.nthatipamula.jslt;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webclient.WebClientResponse;
import io.helidon.webserver.WebServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    private static WebServer webServer;
    private static WebClient webClient;
    private static final JsonBuilderFactory JSON_BUILDER = Json.createBuilderFactory(Collections.emptyMap());
    private static final JsonObject TEST_JSON_OBJECT;

    static {
        TEST_JSON_OBJECT = JSON_BUILDER.createObjectBuilder()
                .add("config", JSON_BUILDER.createObjectBuilder()
                  .add("id", 1)
                  .add("name", "Devicefeatures")
                  .add("transforms", JSON_BUILDER.createArrayBuilder()
                   .add(JSON_BUILDER.createObjectBuilder()
                   .add("name", "device_os")
                   .add("enabled", true)
                   .add("jsltExpression", ".device.osType"))
                ))
                .add("input", JSON_BUILDER.createObjectBuilder()
                  .add("eventID", "123")
                  .add("ip", "123:123:123:123")
                  .add("device", JSON_BUILDER.createObjectBuilder()
                  .add("osType", "Linux")
                  .add("model", "Laptop")
                  )
                )
                .build();
    }

    @BeforeAll
    static void startTheServer() {
        webServer = Main.startServer().await();

        webClient = WebClient.builder()
                .baseUri("http://localhost:" + webServer.port())
                .addMediaSupport(JsonpSupport.create())
                .build();
    }

    @AfterAll
    static void stopServer() throws Exception {
        if (webServer != null) {
            webServer.shutdown()
                    .toCompletableFuture()
                    .get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    void testTransform() {
        JsonObject jsonObject;
        WebClientResponse response;

        jsonObject = webClient.get()
                .path("/transform")
                .request(JsonObject.class)
                .await();
        assertEquals("JSLT Transfor APP !", jsonObject.getString("message"));

        response = webClient.post()
                .path("/transform/retrieveml")
                .submit(TEST_JSON_OBJECT)
                .await();
        assertEquals(200, response.status().code());

        response = webClient.get()
                .path("/health")
                .request()
                .await();
        assertEquals(200, response.status().code());

        response = webClient.get()
                .path("/metrics")
                .request()
                .await();
        assertEquals(200, response.status().code());
    }

}
