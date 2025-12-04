package controler;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.logging.Logger;
import java.util.logging.Level;

@WebServlet("/payment/create-payment-intent")
public class CreatePaymentIntentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CreatePaymentIntentServlet.class.getName());

    static {
        Stripe.apiKey = "sk_test_api_key"; // Any amin'i Tsinjo
        LOGGER.info("Stripe API Key configurée");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.info("Requête reçue pour créer un PaymentIntent");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            String requestBody = sb.toString();
            LOGGER.info("Corps de la requête : " + requestBody);

            if (requestBody == null || requestBody.isEmpty()) {
                LOGGER.warning("Corps de la requête vide");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Corps de la requête vide");
                response.getWriter().write(new Gson().toJson(error));
                return;
            }

            Gson gson = new Gson();
            JsonObject requestData = gson.fromJson(requestBody, JsonObject.class);

            if (!requestData.has("amount") || !requestData.has("currency")) {
                LOGGER.warning("Champs manquants dans la requête");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Champs amount ou currency manquants");
                response.getWriter().write(gson.toJson(error));
                return;
            }

            Long amount = requestData.get("amount").getAsLong();
            String currency = requestData.get("currency").getAsString();

            LOGGER.info("Création PaymentIntent - Montant: " + amount + ", Devise: " + currency);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            LOGGER.info("PaymentIntent créé avec succès - ID: " + paymentIntent.getId());

            JsonObject responseData = new JsonObject();
            responseData.addProperty("clientSecret", paymentIntent.getClientSecret());

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(responseData));

        } catch (StripeException e) {
            LOGGER.log(Level.SEVERE, "Erreur Stripe: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Erreur Stripe: " + e.getMessage());
            error.addProperty("type", e.getClass().getSimpleName());
            response.getWriter().write(new Gson().toJson(error));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur générale: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Erreur serveur: " + e.getMessage());
            response.getWriter().write(new Gson().toJson(error));
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
