package com.example.salon_service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setFrom("lastochkin.kris@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Ошибка отправки письма", e);
        }
    }

    public void sendRegistrationEmail(String to, String name, String login) {
        String subject = "Добро пожаловать в \"Чик-чик\"!";
        String body = EmailTemplates.registrationEmail(name, login);
        sendHtmlEmail(to, subject, body);
    }

    public void sendBookingConfirmationEmail(String to, String name, String date, LocalTime time, String procedure, String master, String price) {
        String subject = "Запись в \"Чик-чик\"";
        String body = EmailTemplates.bookingConfirmation(name, date, time, procedure, price, master);
        sendHtmlEmail(to, subject, body);
    }

    public void sendBookingConfirmationAndRegistrationEmail(String to, String name, String date, LocalTime time, String procedure,
            String master, String price, String login, String passwordResetLink) {

        String subject = "Запись и регистрация в \"Чик-чик\"";
        String body = EmailTemplates.bookingAndRegistration(name, date, time, procedure, price, master, login, passwordResetLink);
        sendHtmlEmail(to, subject, body);
    }

    public void sendBookingCancellationClientEmail(String to, String name, String date, String time, String procedure) {
        String subject = "Отмена записи в \"Чик-чик\"";
        String body = EmailTemplates.cancellationClient(name, date, time, procedure);
        sendHtmlEmail(to, subject, body);
    }

    public void sendBookingCancellationMasterEmail(String to, String name, String date, String time, String procedure) {
        String subject = "Отмена записи в \"Чик-чик\"";
        String body = EmailTemplates.cancellationMaster(name, date, time, procedure);
        sendHtmlEmail(to, subject, body);
    }

    public void sendReminderEmail(String to, String name, String date, String time, String procedure, String master) {
        String subject = "Напоминание о записи в \"Чик-чик\"";
        String body = EmailTemplates.reminder(name, date, time, procedure, master);
        sendHtmlEmail(to, subject, body);
    }

    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        String subject = "Восстановление пароля";
        String content = EmailTemplates.passwordReset(name, resetLink);
        sendHtmlEmail(to, subject, content);
    }

    public void sendPasswordChangedEmail(String to, String name) {
        String subject = "Пароль успешно изменён";
        String content = EmailTemplates.passwordChanged(name);
        sendHtmlEmail(to, subject, content);
    }

}
