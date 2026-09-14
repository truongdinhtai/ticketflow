package com.ticketflow.notification.email;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies real SMTP sending using GreenMail, an in-JVM SMTP server — so the
 * email path is proven without Docker or a real mail provider.
 */
class EmailSenderTest {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Test
    void send_deliversEmailToSmtpServer() throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(ServerSetupTest.SMTP.getPort());

        EmailSender emailSender = new EmailSender(mailSender, "no-reply@ticketflow.local");
        emailSender.send("alice@example.com", "Booking BK-XYZ confirmed", "Hi Alice, your booking is confirmed.");

        assertThat(greenMail.waitForIncomingEmail(3000, 1)).isTrue();
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertThat(received).hasSize(1);
        assertThat(received[0].getSubject()).isEqualTo("Booking BK-XYZ confirmed");
        assertThat(received[0].getAllRecipients()[0].toString()).isEqualTo("alice@example.com");
        assertThat(GreenMailUtil.getBody(received[0])).contains("your booking is confirmed");
    }
}
