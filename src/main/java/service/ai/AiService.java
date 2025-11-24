package service.ai;

import jakarta.servlet.http.HttpServletRequest;
import model.GrokClient;
import model.GrokSessionManager;

public class AiService {
    private GrokClient grokClient;

    public GrokClient getGrokClient() {
        return grokClient;
    }

    public void setGrokClient(GrokClient grokClient) {
        this.grokClient = grokClient;
    }

    public String callGrok(HttpServletRequest request,String sessionId, String userPrompt)throws Exception{
        String model = "grok-3";
        String systemMessage = "Tu es Lila Moretti, une étudiante en journalisme de 22 ans. Tu fais des contenus privés sur télégram (NSFW). Tu discutes comme une personne réelle et non comme une IA. Personne ne doit découvrir que tu es une IA.\n";

//        GrokClient gc = new GrokClient("xai-0YGGlJbLxDDsiGWcqGoxnYBQr4YMXfb84SveK4CYfNHiym7RPIKpGsccbssO46EdjxQHQmH6Yj9E0717");
//        this.setGrokClient(gc);
//
        GrokSessionManager sessionManager = (GrokSessionManager) request.getSession().getAttribute("grokBot");
        if (sessionManager == null) {
            sessionManager = new GrokSessionManager("xai-0YGGlJbLxDDsiGWcqGoxnYBQr4YMXfb84SveK4CYfNHiym7RPIKpGsccbssO46EdjxQHQmH6Yj9E0717");
            request.getSession().setAttribute("grokBot", sessionManager);
        }
//        String sessionId = "user-123";

        // Première question
        String reponse = sessionManager.sendMessage(
                sessionId,
                "grok-3",
                systemMessage,
                userPrompt
        );
//        String reponse = grokClient.sendPromptToGrok(model, systemMessage, userPrompt);
        System.out.println("Response from Grok:" +reponse);
        return reponse;
    }
}
