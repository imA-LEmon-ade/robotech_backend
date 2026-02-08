package com.robotech.robotech_backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendPasswordResetEmail_envia_correo() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:8080");

        emailService.sendPasswordResetEmail("test@robotech.com", "tok");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        assertEquals("test@robotech.com", captor.getValue().getTo()[0]);
    }

    @Test
    void sendPasswordResetEmail_no_lanza_si_falla_envio() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:8080");

        doThrow(new MailException("fail") {}).when(mailSender).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        emailService.sendPasswordResetEmail("test@robotech.com", "tok");
    }
}
