package com.example.salon_service.service;

import com.example.salon_service.entity.Timetable;
import com.example.salon_service.entity.User;
import com.example.salon_service.repository.TimetableRepository;
import com.example.salon_service.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TimetableService {

    private static final Logger logger = LoggerFactory.getLogger(TimetableService.class);

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final LocalTime startTime = LocalTime.of(9, 0);
    private final LocalTime endTime = LocalTime.of(21, 0);

    private final List<Integer> workingDays = List.of(1, 2, 3, 4, 5, 6); // Пн-Сб

    @Scheduled(cron = "0 0 0 * * *") // каждый день в 00:00
    @Transactional
    public void generateTimetableForAllMasters() {
        LocalDate today = LocalDate.now();
        LocalDate lastExistingDate = timetableRepository.findMaxDate(); // найдём последнюю дату в расписании

        List<User> masters = userRepository.findByRoleName("master");
        AtomicInteger createdCount = new AtomicInteger(0);

        // Если расписание пустое — первый запуск. Генерируем 15 рабочих дней с today
        if (lastExistingDate == null) {
            logger.info("Первый запуск генерации расписания. Генерация 15 рабочих дней с {}", today);
            List<LocalDate> initialDates = getNextWorkingDays(today);
            for (User master : masters) {
                for (LocalDate date : initialDates) {
                    createdCount.addAndGet(generateTimetableForDate(master, date));
                }
            }
            logger.info("Первичная генерация завершена. Создано {} слотов", createdCount.get());
        } else {
            // Удаление прошедших слотов
            long deletedCount = timetableRepository.deleteByDateBefore(today);
            logger.info("Удалены слоты до {} (не включительно): {} записей", today, deletedCount);

            // Генерация на today, если это рабочий день и расписания ещё нет
            if (workingDays.contains(today.getDayOfWeek().getValue())) {
                for (User master : masters) {
                    boolean existsToday = timetableRepository.existsByUserIdAndDate(master.getId(), today);
                    if (!existsToday) {
                        createdCount.addAndGet(generateTimetableForDate(master, today));
                    }
                }
            }

            // Генерация недостающих рабочих дней до 15 вперёд
            List<LocalDate> futureWorkingDays = getNextWorkingDays(today);
            for (User master : masters) {
                for (LocalDate date : futureWorkingDays) {
                    boolean slotsExist = timetableRepository.existsByUserIdAndDate(master.getId(), date);
                    if (!slotsExist) {
                        createdCount.addAndGet(generateTimetableForDate(master, date));
                    }
                }
            }
        }

        logger.info("Итого создано {} слотов", createdCount.get());
        logger.info("Расписание обновлено в {}", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0/30 * * * *") // каждые 30 минут
    @Transactional
    public void markPastSlotsAsOld() {
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = timetableRepository.updateStatusToOldForPastSlots(now.toLocalDate(), now.toLocalTime());

        String formattedDate = now.toLocalDate().toString(); // гггг-мм-дд
        String formattedTime = String.format("%02d-%02d", now.getHour(), now.getMinute()); // чч-мм

        logger.info("Закрыты по истечению времени {} окошек на {} в {}", updatedCount, formattedDate, formattedTime);
    }

    // Генерация слотов для конкретной даты
    private int generateTimetableForDate(User master, LocalDate date) {
        int count = 0;
        int intervalMinutes = 30;
        for (LocalTime time = startTime; time.isBefore(endTime); time = time.plusMinutes(intervalMinutes)) {
            Timetable slot = new Timetable();
            slot.setUser(master);
            slot.setDate(date);
            slot.setTime(time);
            slot.setStatus("FREE");
            timetableRepository.save(slot);
            count++;
        }
        return count;
    }

    // Функция для нахождения сгруппированных слотов (для записи)
    public List<LocalTime> findGroupedSlots(Long masterId, LocalDate date, int requiredSlots) {
        List<Timetable> freeSlots = timetableRepository.findByUserIdAndDateAndStatusOrderByTime(masterId, date, "FREE");
        List<LocalTime> result = new ArrayList<>();

        for (int i = 0; i <= freeSlots.size() - requiredSlots; i++) {
            boolean groupOk = true;
            for (int j = 0; j < requiredSlots - 1; j++) {
                LocalTime current = freeSlots.get(i + j).getTime();
                LocalTime next = freeSlots.get(i + j + 1).getTime();
                if (!next.equals(current.plusMinutes(30))) {
                    groupOk = false;
                    break;
                }
            }
            if (groupOk) {
                result.add(freeSlots.get(i).getTime());
            }
        }
        return result;
    }

    private List<LocalDate> getNextWorkingDays(LocalDate fromDate) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate current = fromDate;
        while (result.size() < 14) {
            if (workingDays.contains(current.getDayOfWeek().getValue())) {
                result.add(current);
            }
            current = current.plusDays(1);
        }
        return result;
    }

}