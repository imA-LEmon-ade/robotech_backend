package com.robotech.robotech_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Se recomienda extraer la URL a tu application.properties
    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Async // Permite que el proceso no se detenga esperando el envío del correo
    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Restablecimiento de Contraseña - Robotech");

        // CORRECCIÓN: Se usa \n para el salto de línea y se concatena correctamente
        message.setText("Para restablecer tu contraseña, haz clic en el siguiente enlace (válido por 1 hora):\n"
                + frontendUrl + "/reset-password?token=" + token);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // Usamos log en lugar de System.err para mejores prácticas
            log.error("Error al enviar correo de restablecimiento a {}: {}", to, e.getMessage());
        }
    }
}