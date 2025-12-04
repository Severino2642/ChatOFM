package controler;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/payment/success")
public class PaymentSuccessServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        response.getWriter().write("<!DOCTYPE html><html><head><title>Paiement réussi</title></head>" +
                "<body style='font-family: sans-serif; text-align: center; padding: 50px;'>" +
                "<h1 style='color: #4caf50;'>✓ Paiement réussi !</h1>" +
                "<p>Merci pour votre paiement. Vous allez recevoir une confirmation par email.</p>" +
                "<a href='/' style='color: #667eea;'>Retour à l'accueil</a>" +
                "</body></html>");
    }
}
