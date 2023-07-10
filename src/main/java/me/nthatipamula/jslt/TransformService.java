
package me.nthatipamula.jslt;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.*;

import io.helidon.common.http.Http;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

/**
 * A simple service to greet you. Examples:
 *
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 *
 * Get greeting message for Joe:
 * curl -X GET http://localhost:8080/greet/Joe
 *
 * Change greeting
 * curl -X PUT -H "Content-Type: application/json" -d '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 *
 * The message is returned as a JSON object
 */

public class TransformService implements Service {

    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Collections.emptyMap());

    private static final Logger LOGGER = Logger.getLogger(TransformService.class.getName());

    TransformService() {
    }

    /**
     * A service registers itself by updating the routing rules.
     * @param rules the routing rules.
     */
    @Override
    public void update(Routing.Rules rules) {
        rules
                .get("/", this::getDefaultMessageHandler)
                .post("/retrieveml", this::retrieveMLHandler);
    }



    /**
     * Return a app message.
     * @param request the server request
     * @param response the server response
     */
    private void getDefaultMessageHandler(ServerRequest request, ServerResponse response) {
        sendResponse(response, "JSLT Transfor APP");
    }

    private void sendResponse(ServerResponse response, String name) {
        String msg = String.format("%s !", name);

        JsonObject returnObject = JSON.createObjectBuilder()
                .add("message", msg)
                .build();
        response.send(returnObject);
    }

    private static <T> T processErrors(Throwable ex, ServerRequest request, ServerResponse response) {

        if (ex.getCause() instanceof JsonException){

            LOGGER.log(Level.FINE, "Invalid JSON", ex);
            JsonObject jsonErrorObject = JSON.createObjectBuilder()
                    .add("error", "Invalid JSON")
                    .build();
            response.status(Http.Status.BAD_REQUEST_400).send(jsonErrorObject);
        }  else {

            LOGGER.log(Level.FINE, "Internal error", ex);
            JsonObject jsonErrorObject = JSON.createObjectBuilder()
                    .add("error", "Internal error")
                    .build();
            response.status(Http.Status.INTERNAL_SERVER_ERROR_500).send(jsonErrorObject);
        }

        return null;
    }

    private void retriveMLFeaturesFromJson(JsonObject jo, ServerResponse response) {
        if (!jo.containsKey("config")) {
            JsonObject jsonErrorObject = JSON.createObjectBuilder()
                    .add("error", "No config provided")
                    .build();
            response.status(Http.Status.BAD_REQUEST_400)
                    .send(jsonErrorObject);
            return;
        }
        if (!jo.containsKey("input")) {
            JsonObject jsonErrorObject = JSON.createObjectBuilder()
                    .add("error", "No input provided")
                    .build();
            response.status(Http.Status.BAD_REQUEST_400)
                    .send(jsonErrorObject);
            return;
        }
        JsonObject config = jo.getJsonObject("config");
        JsonArray arr = config.getJsonArray("transforms");

        Iterator<JsonValue> iterator = arr.iterator();
        String configStr  = "{";

        while (iterator.hasNext()) {
            JsonObject jb = (JsonObject) iterator.next();
            if (jb.getBoolean("enabled")) {
                configStr += "\"" + jb.getString("name") + "\" : " + jb.getString("jsltExpression") + ",";
            }
        }
        configStr += "* - ip, sessionId, device:.}";
        String output = JSLTUtils.transformJSON(configStr, jo.getJsonObject("input").toString());
        response.headers().add("Content-Type", "application/json");
        response.send(output);
    }

    /**
     * retrieve the config and process the input to use in future messages.
     * @param request the server request
     * @param response the server response
     */
    private void retrieveMLHandler(ServerRequest request, ServerResponse response) {
        request.content().as(JsonObject.class)
                .thenAccept(jo -> retriveMLFeaturesFromJson(jo, response))
                .exceptionally(ex -> processErrors(ex, request, response));
    }
}