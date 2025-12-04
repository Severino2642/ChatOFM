package controler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ai.AiService;
import service.telegram.TelegramMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@WebServlet(
        name = "TelegramWebhookServlet",
        urlPatterns = "/telegram/webhook",
        loadOnStartup = 1
)
public class TelegramWebhookServlet extends HttpServlet {

    private String botToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Statistiques
    private final AtomicInteger totalMessages = new AtomicInteger(0);
    private final AtomicInteger totalUsers = new AtomicInteger(0);
    private final ConcurrentHashMap<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    private final AiService aiService = new AiService();

    private static class UserSession {
        private Long chatId;
        private String username;
        private String firstName;
        private String language;
        private Date firstSeen;
        private int messageCount;

        public UserSession(Long chatId, String username, String firstName, String language) {
            this.chatId = chatId;
            this.username = username;
            this.firstName = firstName;
            this.language = language;
            this.firstSeen = new Date();
            this.messageCount = 0;
        }

        public void incrementMessageCount() {
            this.messageCount++;
        }

        // Getters
        public Long getChatId() { return chatId; }
        public String getUsername() { return username; }
        public String getFirstName() { return firstName; }
        public String getLanguage() { return language; }
        public Date getFirstSeen() { return firstSeen; }
        public int getMessageCount() { return messageCount; }
    }

    @Override
    public void init() throws ServletException {
        // Récupérer le token
        this.botToken = "8420103338:AAGe5B8ooav4d0n4-MPD6MYWS7Kr0S0wZek";
        if (this.botToken == null || this.botToken.isEmpty()) {
            this.botToken = getServletConfig().getInitParameter("telegramBotToken");
        }

        if (this.botToken == null || this.botToken.isEmpty()) {
            System.err.println("❌ ATTENTION: TELEGRAM_BOT_TOKEN non configuré!");
            System.err.println("ℹ️  Configurez la variable d'environnement ou le paramètre init");
        } else {
            System.out.println("✅ TelegramWebhookServlet initialisé");
            System.out.println("🤖 Bot Token: " + this.botToken.substring(0, 10) + "...");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Vérifier que le token est configuré
        if (this.botToken == null || this.botToken.isEmpty()) {
            sendErrorResponse(response, "Bot token non configuré");
            return;
        }

        try {
            // Log de la requête
            logRequestDetails(request);

            // Lire le corps de la requête
            String requestBody = readRequestBody(request);
            System.out.println("📩 Webhook reçu: " + requestBody);

            // Parser le JSON
            TelegramMessage telegramMessage = objectMapper.readValue(requestBody, TelegramMessage.class);

            // Traiter le message
            if (telegramMessage.getMessage() != null) {
                processMessage(request,telegramMessage.getMessage());
            } else if (telegramMessage.getCallback_query() != null) {
                processCallbackQuery(telegramMessage.getCallback_query());
            }

            // Répondre OK à Telegram
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"ok\"}");

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement webhook: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "Erreur interne: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String html = generateStatusPage();
        response.getWriter().write(html);
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return requestBody.toString();
    }

    private void logRequestDetails(HttpServletRequest request) {
        System.out.println("=== 📨 REQUÊTE WEBHOOK ===");
        System.out.println("🕒 Timestamp: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        System.out.println("🌐 IP: " + request.getRemoteAddr());
        System.out.println("📊 Method: " + request.getMethod());
        System.out.println("==========================");
    }

    private void processMessage(HttpServletRequest request,TelegramMessage.Message message) throws Exception {
        Long chatId = message.getChat().getId();
        String text = message.getText();
        TelegramMessage.From from = message.getFrom();

        String username = from.getUsername() != null ? from.getUsername() : "Sans pseudo";
        String firstName = from.getFirst_name() != null ? from.getFirst_name() : "Utilisateur";
        String language = from.getLanguage_code() != null ? from.getLanguage_code() : "inconnu";

        // Mettre à jour les statistiques
        totalMessages.incrementAndGet();
        UserSession session = userSessions.computeIfAbsent(chatId,
                id -> {
                    totalUsers.incrementAndGet();
                    return new UserSession(chatId, username, firstName, language);
                }
        );
        session.incrementMessageCount();

        System.out.println("💬 Message de " + firstName + " (@" + username + ") [" + language + "]: " + text);

        // Générer et envoyer la réponse
        String sessionId = firstName+username;
        String responseText = generateResponse(request,sessionId,text, session);
        sendTelegramMessage(chatId, responseText);

        // Log de l'activité
        System.out.println("✅ Réponse envoyée à " + firstName);
    }

    private void processCallbackQuery(TelegramMessage.CallbackQuery callbackQuery) {
        // Gérer les interactions de boutons (pour le futur)
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChat().getId();

        System.out.println("🔘 Callback reçu: " + data);
        sendTelegramMessage(chatId, "Bouton cliqué: " + data);
    }

    private String generateResponse(HttpServletRequest request,String sessionId,String text, UserSession session) throws Exception {
        if (text == null || text.trim().isEmpty()) {
            return "❌ Message vide reçu";
        }

        String command = text.toLowerCase().trim();
        String language = session.getLanguage();

        // Adapter la réponse à la langue
//        if ("fr".equals(language)) {
            return generateFrenchResponse(request,sessionId,text,command, session);
//        } else {
//            return generateEnglishResponse(text,command, session);
//        }
    }

    private String generateFrenchResponse(HttpServletRequest request,String sessionId,String text,String command, UserSession session) throws Exception {
        String reponse = "";
        switch (command) {
            case "/start":
                reponse = aiService.callGrok(request,sessionId,"Bonjour");
                return reponse;

            case "/help":
                return "📋 **Centre d'Aide Umano Bot** 🇫🇷\n\n" +
                        "Voici comment m'utiliser:\n\n" +
                        "• /start - Démarrer la conversation\n" +
                        "• /help - Voir cette aide\n" +
                        "• /status - Statut en temps réel\n" +
                        "• /stats - Vos statistiques personnelles\n" +
                        "• /info - Informations techniques\n\n" +
                        "💡 **Astuce:** Vous pouvez simplement m'envoyer un message normal!";

            case "/status":
                return "📊 **Statut Live** 🟢\n\n" +
                        "• 🤖 Bot: **Opérationnel**\n" +
                        "• 👥 Utilisateurs totaux: " + totalUsers.get() + "\n" +
                        "• 📨 Messages traités: " + totalMessages.get() + "\n" +
                        "• 🕒 Uptime: " + getUptime() + "\n" +
                        "• 🌐 Mode: Webhook";

            case "/stats":
                return "👤 **Vos Statistiques**\n\n" +
                        "• Prénom: " + session.getFirstName() + "\n" +
                        "• Pseudo: @" + session.getUsername() + "\n" +
                        "• Langue: " + session.getLanguage() + "\n" +
                        "• Premier message: " + new SimpleDateFormat("dd/MM/yyyy").format(session.getFirstSeen()) + "\n" +
                        "• Messages envoyés: " + session.getMessageCount();

            case "/info":
                return "🤖 **Umano Bot - Informations Techniques**\n\n" +
                        "• 🏗 Architecture: Java Servlet\n" +
                        "• 🚀 Serveur: Apache Tomcat\n" +
                        "• 📡 Communication: Webhook Telegram\n" +
                        "• 💾 Stockage: Mémoire (Session)\n" +
                        "• 🔒 Sécurité: HTTPS + Token\n\n" +
                        "Développé avec ❤️ pour Umano";

            default:
//                String reponse = callDeepSeekAI(command, "sk-88b058b92c6248e782c893efde72af98");
                reponse = aiService.callGrok(request,sessionId,command);
                return reponse;
        }
    }

    private String generateEnglishResponse(String text,String command, UserSession session) {
        switch (command) {
//            case "/start":
//                return "🇺🇸 **Welcome " + session.getFirstName() + "!** 🤖\n\n" +
//                        "I'm your Umano assistant.\n\n" +
//                        "🔧 **Available commands:**\n" +
//                        "/start - Welcome message\n" +
//                        "/help - Help center\n" +
//                        "/status - Bot status\n" +
//                        "/stats - Your statistics\n" +
//                        "/info - Technical information\n\n" +
//                        "Feel free to talk to me!";

            case "/help":
                return "📋 **Umano Bot Help Center** 🇺🇸\n\n" +
                        "Here's how to use me:\n\n" +
                        "• /start - Start conversation\n" +
                        "• /help - See this help\n" +
                        "• /status - Real-time status\n" +
                        "• /stats - Your personal stats\n" +
                        "• /info - Technical information\n\n" +
                        "💡 **Tip:** You can just send me a normal message!";

            case "/status":
                return "📊 **Live Status** 🟢\n\n" +
                        "• 🤖 Bot: **Operational**\n" +
                        "• 👥 Total users: " + totalUsers.get() + "\n" +
                        "• 📨 Processed messages: " + totalMessages.get() + "\n" +
                        "• 🕒 Uptime: " + getUptime() + "\n" +
                        "• 🌐 Mode: Webhook";

            case "/stats":
                return "👤 **Your Statistics**\n\n" +
                        "• First name: " + session.getFirstName() + "\n" +
                        "• Username: @" + session.getUsername() + "\n" +
                        "• Language: " + session.getLanguage() + "\n" +
                        "• First message: " + new SimpleDateFormat("MM/dd/yyyy").format(session.getFirstSeen()) + "\n" +
                        "• Messages sent: " + session.getMessageCount();

            case "/info":
                return "🤖 **Umano Bot - Technical Information**\n\n" +
                        "• 🏗 Architecture: Java Servlet\n" +
                        "• 🚀 Server: Apache Tomcat\n" +
                        "• 📡 Communication: Telegram Webhook\n" +
                        "• 💾 Storage: Memory (Session)\n" +
                        "• 🔒 Security: HTTPS + Token\n\n" +
                        "Developed with ❤️ for Umano";

            default:
                return "💭 **Umano Bot** 🇺🇸\n\n" +
                        "You said: \"" + text + "\"\n\n" +
                        "I'm a simple bot that responds to your messages.\n" +
                        "Type /help to see all available commands.";
        }
    }

    private String getUptime() {
        // Temps depuis le démarrage du servlet (simplifié)
        long startTime = getServletContext().getAttribute("startTime") != null ?
                (Long) getServletContext().getAttribute("startTime") : System.currentTimeMillis();

        long uptime = System.currentTimeMillis() - startTime;
        long hours = uptime / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);

        return hours + "h " + minutes + "m";
    }

    private void sendTelegramMessage(Long chatId, String text) {
        try {
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            // Échapper le texte pour JSON
            String escapedText = text.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            String payload = "{\"chat_id\":" + chatId + ",\"text\":\"" + escapedText + "\",\"parse_mode\":\"Markdown\"}";

            URL telegramUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) telegramUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Message envoyé à " + chatId);
            } else {
                System.err.println("❌ Erreur envoi message. Code: " + responseCode);
                // Lire l'erreur
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println("❌ Détail erreur: " + errorLine);
                    }
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi Telegram: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("{\"status\":\"error\", \"message\":\"" + message + "\"}");
    }

    private String generateStatusPage() {
        boolean isConfigured = (botToken != null && !botToken.isEmpty());

        return "<!DOCTYPE html>" +
                "<html lang='fr'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>Umano Telegram Bot</title>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; margin: 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }" +
                ".container { max-width: 900px; margin: 0 auto; background: rgba(255,255,255,0.1); padding: 30px; border-radius: 15px; backdrop-filter: blur(10px); }" +
                "h1 { text-align: center; margin-bottom: 30px; }" +
                ".status-card { background: rgba(255,255,255,0.2); padding: 20px; border-radius: 10px; margin: 15px 0; }" +
                ".stat { display: flex; justify-content: space-between; margin: 10px 0; }" +
                ".success { color: #90EE90; }" +
                ".warning { color: #FFB6C1; }" +
                ".user-list { max-height: 200px; overflow-y: auto; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<h1>🤖 Umano Telegram Bot - Dashboard</h1>" +

                "<div class='status-card'>" +
                "<h2>📊 Statut du Système</h2>" +
                "<div class='stat'><span>Configuration:</span> <span class='" + (isConfigured ? "success" : "warning") + "'>" +
                (isConfigured ? "✅ Configuré" : "❌ Non configuré") + "</span></div>" +
                "<div class='stat'><span>Utilisateurs actifs:</span> <span>" + totalUsers.get() + "</span></div>" +
                "<div class='stat'><span>Messages traités:</span> <span>" + totalMessages.get() + "</span></div>" +
                "<div class='stat'><span>Uptime:</span> <span>" + getUptime() + "</span></div>" +
                "</div>" +

                "<div class='status-card'>" +
                "<h2>👥 Utilisateurs Actifs</h2>" +
                "<div class='user-list'>" +
                (userSessions.isEmpty() ?
                        "<p>Aucun utilisateur actif</p>" :
                        userSessions.values().stream()
                                .map(session -> "<div class='stat'><span>@" + session.getUsername() + " (" + session.getFirstName() + ")</span><span>" + session.getMessageCount() + " msgs</span></div>")
                                .reduce("", String::concat)) +
                "</div>" +
                "</div>" +

                "<div class='status-card'>" +
                "<h2>🔗 Informations de Connexion</h2>" +
                "<p><strong>URL Webhook:</strong> <code>/telegram/webhook</code></p>" +
                "<p><strong>Méthode:</strong> POST</p>" +
                "<p><strong>Format:</strong> JSON</p>" +
                "<p><strong>Statut:</strong> 🟢 Opérationnel</p>" +
                "</div>" +

                "</div>" +
                "</body>" +
                "</html>";
    }

    @Override
    public void destroy() {
        System.out.println("🛑 TelegramWebhookServlet arrêté");
        System.out.println("📊 Statistiques finales:");
        System.out.println("• Utilisateurs: " + totalUsers.get());
        System.out.println("• Messages: " + totalMessages.get());
    }

    public static String callDeepSeekAI(String prompt, String apiKey) throws Exception {
        String apiUrl = "https://api.deepseek.com/v1/chat/completions";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);

        String requestBody = String.format("{\n" +
                "  \"model\": \"deepseek-chat\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"You are a helpful assistant.\"},\n" +
                "    {\"role\": \"user\", \"content\": \"%s\"}\n" +
                "  ]\n" +
                "}", prompt.replace("\"", "\\\""));

        System.out.println("Request Body: " + requestBody);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode == 200 ? connection.getInputStream() : connection.getErrorStream(),
                StandardCharsets.UTF_8
        ));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }
        br.close();
        connection.disconnect();

        String jsonResponse = response.toString();
        System.out.println("Raw Response: " + jsonResponse);

        return extractDeepSeekResponse(jsonResponse);
    }

    private static String extractDeepSeekResponse(String jsonResponse) throws Exception {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

        JsonArray choices = jsonObject.getAsJsonArray("choices");
        if (choices != null && choices.size() > 0) {
            JsonObject message = choices
                    .get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message");

            if (message != null && message.has("content")) {
                return message.get("content").getAsString();
            }
        }

        return "No response content.";
    }
}
