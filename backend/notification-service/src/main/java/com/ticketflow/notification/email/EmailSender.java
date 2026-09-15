package com.ticketflow.notification.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends plain-text emails via the configured SMTP server. Locally this points
 * at MailHog (a fake SMTP server with a web UI), so emails are captured for
 * inspection rather than delivered to real inboxes.
 */
@Component
public class EmailSender {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailSender(JavaMailSender mailSender,
                       @Value("${ticketflow.mail.from:no-reply@ticketflow.local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
