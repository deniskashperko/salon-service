package com.example.salon_service.mail;
import java.time.LocalTime;

public class EmailTemplates {

    public static String registrationEmail(String name, String login) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Добрый день, %s! 👋</p>
            <p>Вы успешно зарегистрированы на нашем сайте.<br>
            В личном кабинете клиента Вы можете отслеживать свои записи, а также управлять ими.<br>
            Ваш логин: <strong>%s</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Если Вы забыли пароль, воспользуйтесь формой восстановления пароля на нашем сайте.</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>Будем рады видеть Вас! 😊</p>
            </body></html>
        """.formatted(name, login);
    }

    public static String bookingConfirmation(String name, String date, LocalTime time, String procedure, String price, String master) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Добрый день, %s! 👋</p>
            <p>Вы успешно записаны к нам в салон <strong>%s</strong> в <strong>%s</strong>.<br>
            Услуга: <strong>%s</strong>, %s ₽<br>
            Мастер: <strong>%s</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Управлять своими записями Вы можете в личном кабинете на нашем сайте.<br>
            Если Вы забыли пароль, воспользуйтесь формой восстановления пароля.</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>Ждём Вас! 😊</p>
            </body></html>
        """.formatted(name, date, time, procedure, price, master);
    }

    public static String bookingAndRegistration(String name, String date, LocalTime time, String procedure, String price, String master, String login, String passwordResetLink) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Добрый день, %s! 👋</p>
            <p>Вы успешно записаны к нам в салон <strong>%s</strong> в <strong>%s</strong>.<br>
            Услуга: <strong>%s</strong>, %s ₽<br>
            Мастер: <strong>%s</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Ваш аккаунт был зарегистрирован автоматически.<br>
            Ваш логин: <strong>%s</strong><br>
            Чтобы задать пароль, перейдите по ссылке: <a href="%s">восстановить пароль</a></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Если Вы забудете пароль, воспользуйтесь формой восстановления пароля.</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>Ждём Вас! 😊</p>
            </body></html>
        """.formatted(name, date, time, procedure, price, master, login, passwordResetLink);
    }

    public static String cancellationClient(String name, String date, String time, String procedure) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>%s, добрый день! 👋</p>
            <p>Вы отменили запись <strong>%s</strong> в <strong>%s</strong>, услуга <strong>%s</strong>.</p>
            <p>Если это произошло по ошибке — свяжитесь с нами или создайте новую заявку.</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>Надеемся увидеть Вас снова! 😊</p>
            </body></html>
        """.formatted(name, date, time, procedure);
    }

    public static String cancellationMaster(String name, String date, String time, String procedure) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>%s, добрый день! 👋</p>
            <p>Сотрудник отменил Вашу запись <strong>%s</strong> в <strong>%s</strong>, услуга <strong>%s</strong>.</p>
            <p>Если это произошло по ошибке — свяжитесь с нами или создайте новую заявку.</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>Надеемся увидеть Вас снова! 😊</p>
            </body></html>
        """.formatted(name, date, time, procedure);
    }

    public static String reminder(String name, String date, String time, String procedure, String master) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>%s, добрый день! 👋</p>
            <p>Напоминаем, что Вы записаны завтра, <strong>%s</strong> в <strong>%s</strong>.<br>
            Услуга: <strong>%s</strong><br>
            Мастер: <strong>%s</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>Если вы не сможете прийти — пожалуйста, отмените запись или позвоните нам.<br>
            Спасибо! 😊</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>До скорой встречи!</p>
            </body></html>
        """.formatted(name, date, time, procedure, master);
    }

    public static String passwordReset(String name, String link) {
        return """
            <html><body style='font-family: Arial, sans-serif;'>
            <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
            <hr style="border-top: 1px solid #ccc">
            <p>%s, добрый день! 👋</p>
            <p>Вы запросили сброс пароля для своей учётной записи.</p>
            <p>Для восстановления пароля перейдите по ссылке ниже. Ссылка действительна 30 минут:</p>
            <p><a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #dc3545; color: #fff; text-decoration: none; border-radius: 4px;">Сбросить пароль</a></p>
            <p>Если вы не запрашивали восстановление пароля, просто проигнорируйте это письмо.</p>
            <p>С уважением,<br>Салон "Чик-Чик"</p>
            <hr style="border-top: 1px solid #ccc">
            <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
            <p>До скорой встречи!</p>
            </body></html>
        """.formatted(name, link);
    }

    public static String passwordChanged(String name) {
        return """
        <html><body style='font-family: Arial, sans-serif;'>
        <p><strong>Парикмахерский салон "Чик-чик"</strong></p>
        <hr style="border-top: 1px solid #ccc">
        <p>%s, добрый день! 👋</p>
        <p>Ваш пароль был успешно изменён.</p>
        <p>Если Вы не производили это действие, пожалуйста, немедленно свяжитесь с нами.</p>
        <hr style="border-top: 1px solid #ccc">
        <p>Вы всегда можете восстановить доступ с помощью формы восстановления пароля на нашем сайте.</p>
        <hr style="border-top: 1px solid #ccc">
        <p>Адрес: г. Обь, ул. Геодезическая, 8<br>Телефон: +7 (951) 397-95-28</p>
        <p>С заботой о Вас, команда "Чик-Чик" 😊</p>
        </body></html>
    """.formatted(name);
    }



}
