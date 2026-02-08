package com.robotech.robotech_backend.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.InputStream;
import java.util.Properties;

@TestConfiguration
public class TestMailConfig {

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                return new MimeMessage((Session) null);
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                try {
                    return new MimeMessage(Session.getDefaultInstance(new Properties()), contentStream);
                } catch (Exception e) {
                    throw new MailParseException(e);
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                // no-op
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
                // no-op
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
                try {
                    mimeMessagePreparator.prepare(createMimeMessage());
                } catch (Exception e) {
                    throw new MailPreparationException(e);
                }
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
                for (MimeMessagePreparator preparator : mimeMessagePreparators) {
                    send(preparator);
                }
            }

            @Override
            public void send(org.springframework.mail.SimpleMailMessage simpleMessage) throws MailException {
                // no-op
            }

            @Override
            public void send(org.springframework.mail.SimpleMailMessage... simpleMessages) throws MailException {
                // no-op
            }
        };
    }
}
