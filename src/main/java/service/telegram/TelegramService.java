package service.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import service.test.BusinessService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramService {
    private final String botToken;
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public TelegramService(String botToken) {
        this.botToken = botToken;
        this.apiUrl = "https://api.telegram.org/bot" + botToken + "/";
        this.objectMapper = new ObjectMapper();
    }

    // Méthode pour envoyer un message
    public void sendMessage(Long chatId, String text) {
        try {
            String url = apiUrl + "sendMessage";
            String payload = String.format(
                    "{\"chat_id\":%d,\"text\":\"%s\"}",
                    chatId,
                    escapeJsonString(text)
            );

            sendPostRequest(url, payload);

        } catch (Exception e) {
            System.err.println("Erreur envoi message: " + e.getMessage());
        }
    }

    // Méthode pour traiter les messages entrants
    public void processMessage(TelegramMessage message) {
        if (message.getMessage() == null) return;

        Long chatId = message.getMessage().getChat().getId();
        String userMessage = message.getMessage().getText();
        String username = message.getMessage().getFrom().getUsername();

        System.out.println("Message reçu de " + username + ": " + userMessage);

        // Traitement par le service métier
        BusinessService businessService = new BusinessService();
        String response = businessService.processMessage(userMessage);

        // Envoi de la réponse
        sendMessage(chatId, response);
    }

    // Méthode utilitaire pour envoyer des requêtes POST
    private void sendPostRequest(String urlString, String payload) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.err.println("Erreur HTTP: " + responseCode);
        }

        connection.disconnect();
    }

    // Échapper les caractères spéciaux pour JSON
    private String escapeJsonString(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
