package com.stan.blog.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTests {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(emailService, "mailFrom", "no-reply@example.com");
    }

    @Test
    void sendTemplateEmailRendersTemplateAndSendsMessage() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(templateEngine.process(eq("welcome"), any())).thenReturn("<p>Hello</p>");
        when(mailSender.createMimeMessage()).thenReturn(message);
        doNothing().when(mailSender).send(message);

        emailService.sendTemplateEmail("alice@example.com", "Subject", "welcome", Map.of("name", "Alice"));

        verify(mailSender).send(message);
        assertEquals("Subject", message.getSubject());
        assertEquals("no-reply@example.com", message.getFrom()[0].toString());
    }

    @Test
    void sendTemplateEmailWrapsMessagingException() throws Exception {
        MimeMessage faulty = new MimeMessage(Session.getInstance(new Properties())) {
            @Override
            public void setRecipients(Message.RecipientType type, Address[] addresses) throws MessagingException {
                throw new MessagingException("cannot set recipient");
            }
        };
        when(templateEngine.process(eq("reset"), any())).thenReturn("reset");
        when(mailSender.createMimeMessage()).thenReturn(faulty);

        assertThrows(RuntimeException.class, () ->
            emailService.sendTemplateEmail("bob@example.com", "Subject", "reset", Map.of()));
    }
}
