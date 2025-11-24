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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrokChatService {
    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private List<Map<String, String>> conversationHistory = new ArrayList<>();

    public GrokChatService(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    public String sendPromptToGrok(String model, String systemMessage, String userPrompt) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode messages = mapper.createArrayNode();

        // Ajouter le message système une seule fois au début
        if (systemMessage != null && !systemMessage.isBlank() && conversationHistory.isEmpty()) {
            ObjectNode sys = mapper.createObjectNode();
            sys.put("role", "system");
            sys.put("content", systemMessage);
            messages.add(sys);

            // Sauvegarder dans l'historique
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            conversationHistory.add(systemMsg);
        }

        // Ajouter tout l'historique de conversation
        for (Map<String, String> msg : conversationHistory) {
            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("role", msg.get("role"));
            messageNode.put("content", msg.get("content"));
            messages.add(messageNode);
        }

        // Ajouter le nouveau message utilisateur
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
        String assistantResponse = extractResponse(json);

        // Sauvegarder le message utilisateur et la réponse dans l'historique
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        conversationHistory.add(userMsg);

        Map<String, String> assistantMsg = new HashMap<>();
        assistantMsg.put("role", "assistant");
        assistantMsg.put("content", assistantResponse);
        conversationHistory.add(assistantMsg);

        return assistantResponse;
    }

    public void clearConversationHistory() {
        conversationHistory.clear();
    }

    public List<Map<String, String>> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    public void setConversationHistory(List<Map<String, String>> history) {
        this.conversationHistory = new ArrayList<>(history);
    }

    // Méthode pour limiter la taille de l'historique (éviter les tokens excessifs)
    public void trimConversationHistory(int maxMessages) {
        if (conversationHistory.size() > maxMessages) {
            // Garder le message système et les messages les plus récents
            List<Map<String, String>> systemMessages = conversationHistory.stream()
                    .filter(msg -> "system".equals(msg.get("role")))
                    .collect(Collectors.toList());

            List<Map<String, String>> recentMessages = conversationHistory.stream()
                    .filter(msg -> !"system".equals(msg.get("role")))
                    .skip(conversationHistory.size() - maxMessages + systemMessages.size())
                    .collect(Collectors.toList());

            conversationHistory = new ArrayList<>();
            conversationHistory.addAll(systemMessages);
            conversationHistory.addAll(recentMessages);
        }
    }

    private String extractResponse(JsonNode json) {
        // Votre logique d'extraction existante
        if (json.has("choices") && json.get("choices").isArray()) {
            JsonNode first = json.get("choices").get(0);
            if (first != null) {
                JsonNode message = first.path("message");
                if (!message.isMissingNode()) {
                    JsonNode content = message.path("content");
                    if (content.isTextual()) {
                        return content.asText();
                    }
                }
            }
        }
        return json.toString();
    }
}
