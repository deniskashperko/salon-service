package com.example.salon_service.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final TimetableService timetableService;
    private final RequestService requestService;

    @Autowired
    public StartupRunner(TimetableService timetableService, RequestService requestService) {
        this.timetableService = timetableService;
        this.requestService = requestService;
    }

    @Override
    public void run(String... args) {
        timetableService.markPastSlotsAsOld();              // закрывает старые окошки
        timetableService.generateTimetableForAllMasters();  // обновляет расписание
        requestService.updateRequestStatus();               // обновляет статусы заявок
        requestService.sendDailyReminders();                // отправляет напоминания о заявках
    }
}
