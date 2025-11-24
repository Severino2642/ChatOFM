package model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class GrokClient {

    private final HttpClient http;
    private final String apiKey;
    private final ObjectMapper mapper;

    public GrokClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.mapper = new ObjectMapper();
    }

    public String sendPromptToGrok(String model, String systemMessage, String userPrompt) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode messages = mapper.createArrayNode();

        if (systemMessage != null && !systemMessage.isBlank()) {
            ObjectNode sys = mapper.createObjectNode();
            sys.put("role", "system");
            sys.put("content", systemMessage);
            messages.add(sys);
        }

        ObjectNode user = mapper.createObjectNode();
        user.put("role", "user");
        user.put("content", userPrompt);
        messages.add(user);

        root.set("messages", messages);
        root.put("model", model);
        root.put("stream", false);

        String requestBody = mapper.writeValueAsString(root);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.x.ai/v1/chat/completions"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();
        String body = response.body();

        if (status / 100 != 2) {
            throw new RuntimeException("Erreur API: HTTP " + status + " -> " + body);
        }

        JsonNode json = mapper.readTree(body);

        if (json.has("choices") && json.get("choices").isArray()) {
            JsonNode first = json.get("choices").get(0);
            if (first != null) {
                JsonNode message = first.path("message");
                if (!message.isMissingNode()) {
                    JsonNode content = message.path("content");
                    if (content.isTextual()) {
                        return content.asText();
                    }
                    JsonNode text = content.path("text");
                    if (text.isTextual()) return text.asText();
                }
                JsonNode textNode = first.path("text");
                if (textNode.isTextual()) return textNode.asText();
            }
        }

        if (json.has("output")) {
            JsonNode out = json.get("output");
            if (out.isArray() && out.size() > 0 && out.get(0).isTextual()) {
                return out.get(0).asText();
            }
            if (out.isTextual()) return out.asText();
        }

        return body;
    }
}

