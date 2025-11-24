package service.test;

import model.GrokSessionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BusinessService {

    public String processMessage(String message) {
        if (message == null) {
            return "Message vide reçu";
        }

        String lowerMessage = message.toLowerCase().trim();

        switch (lowerMessage) {
            case "/start":
                return "🚀 Bienvenue dans mon Bot Telegram !\n\n" +
                        "Commandes disponibles:\n" +
                        "/start - Démarrer le bot\n" +
                        "/help - Aide\n" +
                        "/time - Heure actuelle\n" +
                        "/info - Informations";

            case "/help":
                return "📋 **Aide**\n\n" +
                        "Voici les commandes disponibles:\n" +
                        "• /start - Démarrer le bot\n" +
                        "• /help - Voir cette aide\n" +
                        "• /time - Heure actuelle\n" +
                        "• /info - Informations système";

            case "/time":
                String time = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                );
                return "🕐 Heure actuelle: " + time;

            case "/info":
                return "🤖 **Informations Bot**\n\n" +
                        "• Développé en Java Servlet\n" +
                        "• API Telegram Bot\n" +
                        "• Serveur: " + System.getProperty("os.name") + "\n" +
                        "• Mémoire: " + getMemoryInfo();

            default:
                return "❓ Je n'ai pas compris: \"" + message + "\"\n\n" +
                        "Tapez /help pour voir les commandes disponibles.";
        }
    }

    private String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        return usedMemory + "MB / " + maxMemory + "MB";
    }

    public static void main(String[] args) throws Exception {
        GrokSessionManager sessionManager = new GrokSessionManager("xai-0YGGlJbLxDDsiGWcqGoxnYBQr4YMXfb84SveK4CYfNHiym7RPIKpGsccbssO46EdjxQHQmH6Yj9E0717");

        String sessionId = "user-123";
        String systemMessage = "Vous êtes un assistant utile. Répondez en français.";

        // Première question
        String response1 = sessionManager.sendMessage(
                sessionId,
                "grok-3",
                systemMessage,
                "Bonjour, comment ça va ?"
        );
        System.out.println("Réponse 1: " + response1);

        // Deuxième question - Grok se souviendra de la conversation
        String response2 = sessionManager.sendMessage(
                sessionId,
                "grok-3",
                null, // Le message système est déjà dans l'historique
                "Qu'est-ce que je t'ai demandé précédemment ?"
        );
        System.out.println("Réponse 2: " + response2);
    }
}
