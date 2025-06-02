package com.example.salon_service.controller;

import com.example.salon_service.entity.Request;
import com.example.salon_service.mail.EmailService;
import com.example.salon_service.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.format.TextStyle;
import java.util.Locale;

@Controller
@RequestMapping({"/lkclient", "/lkemployee"})
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private EmailService emailService;

    // Обработчик для отмены заявки клиентом
    @PostMapping("/cancel-request")
    public String cancelRequest(@RequestParam Long requestId) {
        Request request = requestService.findById(requestId);
        boolean isCancelled = requestService.cancelRequest(requestId);
        if (isCancelled) {
            sendCancellationEmail(request, true); // отмена клиентом
        }
        return isCancelled ? "redirect:/lkclient" : "redirect:/lkclient?error";
    }

    // Обработчик для отмены заявки сотрудником
    @PostMapping("/employee-cancel-request")
    public String cancelRequestEmployee(@RequestParam Long requestId) {
        Request request = requestService.findById(requestId);
        boolean isCancelled = requestService.cancelRequest(requestId);
        if (isCancelled) {
            sendCancellationEmail(request, false); // отмена сотрудником
        }
        return isCancelled ? "redirect:/lkemployee/records" : "redirect:/lkemployee/records?error";
    }

    // Метод для отправки письма об отмене
    private void sendCancellationEmail(Request request, boolean byClient) {
        if (request.getClient() != null && request.getClient().getContact() != null) {
            String email = request.getClient().getContact().getEmail();
            if (email != null && !email.isBlank()) {
                String name = request.getClient().getName();

                // Форматируем дату в стиле "24 мая"
                Locale russian = new Locale("ru");
                String formattedDate = request.getDate().getDayOfMonth() + " " +
                        request.getDate().getMonth().getDisplayName(TextStyle.FULL, russian);

                String time = request.getTime().toString();
                String procedure = request.getProcedure().getName();

                if (byClient) {
                    emailService.sendBookingCancellationClientEmail(email, name, formattedDate, time, procedure);
                } else {
                    emailService.sendBookingCancellationMasterEmail(email, name, formattedDate, time, procedure);
                }
            }
        }
    }

}
