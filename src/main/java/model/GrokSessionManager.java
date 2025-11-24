package model;

import service.ai.AiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GrokSessionManager {
    private final Map<String, List<Map<String, String>>> sessions = new ConcurrentHashMap<>();
    private final GrokChatService grokService;

    public GrokSessionManager(String apiKey) {
        this.grokService = new GrokChatService(apiKey);
    }

    public String sendMessage(String sessionId, String model, String systemMessage, String userPrompt) throws Exception {
        List<Map<String, String>> sessionHistory = sessions.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Si c'est une nouvelle session, ajouter le message système
        if (sessionHistory.isEmpty() && systemMessage != null && !systemMessage.isBlank()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemMessage);
            sessionHistory.add(systemMsg);
        }

        grokService.setConversationHistory(sessionHistory);
        String response = grokService.sendPromptToGrok(model, null, userPrompt);

        // Mettre à jour la session avec la nouvelle histoire
        sessions.put(sessionId, grokService.getConversationHistory());

        return response;
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public List<Map<String, String>> getSessionHistory(String sessionId) {
        return sessions.getOrDefault(sessionId, new ArrayList<>());
    }
}
