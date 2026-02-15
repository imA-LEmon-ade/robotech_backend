package com.robotech.robotech_backend.service;

import com.robotech.robotech_backend.exception.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.resend.com")
            .build();

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from:onboarding@resend.dev}")
    private String resendFrom;

    public void sendPasswordResetEmail(String to, String token) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new EmailDeliveryException("RESEND_API_KEY no configurado en el backend");
        }

        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String html = "<p>Para restablecer tu contrasena, haz clic en el siguiente enlace (valido por 1 hora):</p>"
                + "<p><a href=\"" + resetUrl + "\">Restablecer contrasena</a></p>";

        Map<String, Object> payload = Map.of(
                "from", resendFrom,
                "to", to,
                "subject", "Restablecimiento de Contrasena - Robotech",
                "html", html
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Correo de recuperacion enviado a {}", to);
        } catch (RestClientResponseException e) {
            String detail = String.format("Resend rechazo el envio (status=%s): %s",
                    e.getStatusCode(), e.getResponseBodyAsString());
            log.error("{}", detail);
            throw new EmailDeliveryException(detail);
        } catch (Exception e) {
            log.error("Error al enviar correo de restablecimiento a {}: {}", to, e.getMessage());
            throw new EmailDeliveryException("No se pudo enviar el correo de restablecimiento");
        }
    }
}
