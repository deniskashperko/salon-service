package com.example.salon_service.service;

import com.example.salon_service.entity.Request;
import com.example.salon_service.entity.Timetable;
import com.example.salon_service.entity.User;
import com.example.salon_service.mail.EmailService;
import com.example.salon_service.repository.RequestRepository;
import com.example.salon_service.repository.TimetableRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class RequestService {
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    @Autowired private RequestRepository requestRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private EmailService emailService;

    // Получаем список заявок для клиента по объекту или по ID
    public List<Request> getRequestsForClient(Object client) {
        Long clientId = null;

        // Проверяем тип клиента (User или Long)
        if (client instanceof User) {
            clientId = ((User) client).getId();
        } else if (client instanceof Long) {
            clientId = (Long) client;
        } else {
            throw new IllegalArgumentException("Неверный тип клиента");
        }

        List<Request> requests = requestRepository.findSortedByClientId(clientId);
        logger.info("Найдено {} заявок для клиента с ID {}", requests.size(), clientId);
        return requests;
    }

    // Получаем список заявок для мастера
    public List<Request> getRequestsForMaster(Long masterId) {
        return requestRepository.findSortedByMasterId(masterId);
    }

    // Получаем заявку по ID
    public Request findById(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
    }

    // Обновляем заявку
    public void save(Request request) {
        requestRepository.save(request);
    }

    // Метод для отмены заявки
    public boolean cancelRequest(Long requestId) {
        Optional<Request> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isPresent()) {
            Request request = requestOpt.get();
            if ("AWAIT".equals(request.getStatus())) {
                request.setStatus("CANCELLED");
                requestRepository.save(request);

                // Отвязываем слоты
                List<Timetable> relatedSlots = timetableRepository.findAllByRequestId(requestId);
                for (Timetable slot : relatedSlots) {
                    slot.setStatus("FREE");
                    slot.setRequestId(null);
                    timetableRepository.save(slot);
                }
                logger.info("Заявка ID {} отменена. {} слотов освобождено.", requestId, relatedSlots.size());

                return true;
            }
        }
        return false;
    }

    // Метод для обновления статусов заявок
    @Scheduled(cron = "0 0/30 * * * *") // каждые 30 минут
    public void updateRequestStatus() {
        LocalDateTime now = LocalDateTime.now();
        Iterable<Request> requests = requestRepository.findAll();
        for (Request request : requests) {
            if ("DONE".equals(request.getStatus())) {
                continue;
            }
            int durationInMinutes = request.getProcedure().getExTime().getHour() * 60
                    + request.getProcedure().getExTime().getMinute();
            LocalDateTime requestEndTime = request.getDate().atTime(request.getTime().getHour(),
                            request.getTime().getMinute())
                    .plusMinutes(durationInMinutes);
            if (now.isAfter(requestEndTime)) {
                request.setStatus("DONE");
                requestRepository.save(request);  // Сохраняем изменения
                logger.info("Заявка с ID {} изменена на статус 'DONE'.", request.getId());
            }
        }
        logger.info("Статусы всех заявок обновлены.");
    }

    // Метод для рассылки напоминаний о заявках
    @Scheduled(cron = "0 0 12 * * *") // каждый день в 12:00
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Request> requestsForTomorrow = requestRepository.findAllByStatusAndDate("AWAIT", tomorrow);

        for (Request request : requestsForTomorrow) {
            User client = request.getClient();
            if (client != null && client.getContact() != null) {
                String email = client.getContact().getEmail();
                if (email != null && !email.isBlank()) {
                    try {
                        emailService.sendReminderEmail(
                                email,
                                client.getName(),
                                formatDate(request.getDate()),
                                request.getTime().toString(),
                                request.getProcedure().getName(),
                                request.getMaster().getName()
                        );
                    } catch (Exception e) {
                        logger.error("Ошибка при отправке письма-напоминания для заявки {}: {}", request.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    private String formatDate(LocalDate date) {
        Locale russian = new Locale("ru");
        return date.getDayOfMonth() + " " + date.getMonth().getDisplayName(java.time.format.TextStyle.FULL, russian);
    }


}
