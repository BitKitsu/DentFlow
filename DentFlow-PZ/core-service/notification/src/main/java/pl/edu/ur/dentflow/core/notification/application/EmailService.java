package pl.edu.ur.dentflow.core.notification.application;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending emails via SendGrid (using Spring Mail + SMTP).
 * SCRUM-65
 *
 * Configuration in application.yml:
 * spring.mail.host: smtp.sendgrid.net
 * spring.mail.port: 587
 * spring.mail.username: apikey
 * spring.mail.password: ${SENDGRID_API_KEY}
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final JavaMailSender mailSender;

    @Value("${mail.from:noreply@dentflow.pl}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends appointment confirmation to the patient.
     *
     * @param toEmail     patient email address
     * @param patientName patient full name
     * @param clinicName  clinic name
     * @param dentistName dentist full name
     * @param startAt     appointment date and time
     * @param location    location name
     */
    public void sendAppointmentConfirmation(String toEmail,
            String patientName,
            String clinicName,
            String dentistName,
            OffsetDateTime startAt,
            String location) {
        String subject = "Potwierdzenie wizyty – " + clinicName;
        String body = buildConfirmationBody(patientName, clinicName, dentistName, startAt, location);
        sendEmail(toEmail, subject, body);
        log.info("Appointment confirmation sent to: {}", toEmail);
    }

    /**
     * Sends appointment reminder (e.g. 24h before the appointment).
     *
     * @param toEmail     patient email address
     * @param patientName patient full name
     * @param clinicName  clinic name
     * @param dentistName dentist full name
     * @param startAt     appointment date and time
     * @param location    location name
     */
    public void sendAppointmentReminder(String toEmail,
            String patientName,
            String clinicName,
            String dentistName,
            OffsetDateTime startAt,
            String location) {
        String subject = "Przypomnienie o wizycie – " + startAt.format(FORMATTER);
        String body = buildReminderBody(patientName, clinicName, dentistName, startAt, location);
        sendEmail(toEmail, subject, body);
        log.info("Appointment reminder sent to: {}", toEmail);
    }

    /**
     * Sends appointment cancellation notification.
     */
    public void sendAppointmentCancellation(String toEmail,
            String patientName,
            String clinicName,
            OffsetDateTime startAt) {
        String subject = "Anulowanie wizyty – " + clinicName;
        String body = buildCancellationBody(patientName, clinicName, startAt);
        sendEmail(toEmail, subject, body);
        log.info("Appointment cancellation notification sent to: {}", toEmail);
    }

    // private helpers

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
            // We do not throw - email is a helper function, not blocking main logic
        }
    }

    private String buildConfirmationBody(String patient, String clinic,
            String dentist, OffsetDateTime startAt, String location) {
        return """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                  <h2 style="color: #2563eb;">DentFlow – Potwierdzenie wizyty</h2>
                  <p>Drogi/a <strong>%s</strong>,</p>
                  <p>Potwierdzamy Twoją wizytę w gabinecie <strong>%s</strong>.</p>
                  <table style="border-collapse: collapse; margin-top: 16px;">
                    <tr><td style="padding: 6px 12px; font-weight: bold;">Termin:</td>
                        <td style="padding: 6px 12px;">%s</td></tr>
                    <tr><td style="padding: 6px 12px; font-weight: bold;">Lekarz:</td>
                        <td style="padding: 6px 12px;">%s</td></tr>
                    <tr><td style="padding: 6px 12px; font-weight: bold;">Lokalizacja:</td>
                        <td style="padding: 6px 12px;">%s</td></tr>
                  </table>
                  <p style="margin-top: 24px; font-size: 12px; color: #666;">
                    W razie pytań skontaktuj się z gabinetem.<br/>
                    Zespół DentFlow
                  </p>
                </body></html>
                """.formatted(patient, clinic, startAt.format(FORMATTER), dentist, location);
    }

    private String buildReminderBody(String patient, String clinic,
            String dentist, OffsetDateTime startAt, String location) {
        return """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                  <h2 style="color: #f59e0b;">DentFlow – Przypomnienie o wizycie</h2>
                  <p>Drogi/a <strong>%s</strong>,</p>
                  <p>Przypominamy o jutrzejszej wizycie w gabinecie <strong>%s</strong>.</p>
                  <table style="border-collapse: collapse; margin-top: 16px;">
                    <tr><td style="padding: 6px 12px; font-weight: bold;">Termin:</td>
                        <td style="padding: 6px 12px;">%s</td></tr>
                    <tr><td style="padding: 6px 12px; font-weight: bold;">Lekarz:</td>
                        <td style="padding: 6px 12px;">%s</td></tr>
                    <tr><td style="padding: 6px 12px; font-weight: bold;">Lokalizacja:</td>
                        <td style="padding: 6px 12px;">%s</td></tr>
                  </table>
                  <p style="margin-top: 24px; font-size: 12px; color: #666;">Zespół DentFlow</p>
                </body></html>
                """.formatted(patient, clinic, startAt.format(FORMATTER), dentist, location);
    }

    private String buildCancellationBody(String patient, String clinic, OffsetDateTime startAt) {
        return """
                <html><body style="font-family: Arial, sans-serif; color: #333;">
                  <h2 style="color: #dc2626;">DentFlow – Anulowanie wizyty</h2>
                  <p>Drogi/a <strong>%s</strong>,</p>
                  <p>Informujemy, że Twoja wizyta w gabinecie <strong>%s</strong>
                     zaplanowana na <strong>%s</strong> została anulowana.</p>
                  <p>Skontaktuj się z gabinetem, aby umówić nowy termin.</p>
                  <p style="margin-top: 24px; font-size: 12px; color: #666;">Zespół DentFlow</p>
                </body></html>
                """.formatted(patient, clinic, startAt.format(FORMATTER));
    }
}
