package com.example.salon_service.controller;
import com.example.salon_service.entity.*;
import com.example.salon_service.mail.EmailService;
import com.example.salon_service.repository.*;
import com.example.salon_service.service.TimetableService;
import com.example.salon_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.security.Principal;
import java.time.format.TextStyle;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class BookingController {
    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private ProcedureRepository procedureRepository;
    @Autowired private RequestRepository requestRepository;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private TimetableService timetableService;
    @Autowired private EmailService emailService;

    @GetMapping("/booking")
    public String showBookingPage(Model model) {
        List<Procedure> procedures = procedureRepository.findAll();
        List<User> masters = userRepository.findByRoleName("master");
        model.addAttribute("procedures", procedures);
        model.addAttribute("masters", masters);
        return "public/booking";
    }

    @GetMapping("/api/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @RequestParam Long masterId,
            @RequestParam Long procedureId) {

        Procedure procedure = procedureRepository.findById(procedureId).orElseThrow();
        int requiredSlots = procedure.getExTime().toSecondOfDay() / (30 * 60);

        List<LocalDate> availableDates = timetableRepository.findAvailableDates(masterId, requiredSlots);
        return ResponseEntity.ok(availableDates);
    }

    @GetMapping("/api/available-slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots
            (@RequestParam Long masterId, @RequestParam Long procedureId,
             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Procedure procedure = procedureRepository.findById(procedureId).orElseThrow();
        int requiredSlots = procedure.getExTime().toSecondOfDay() / (30 * 60);

        List<LocalTime> availableTimes = timetableService.findGroupedSlots(masterId, date, requiredSlots);
        return ResponseEntity.ok(availableTimes);
    }

    @GetMapping("/api/masters-by-procedure")
    @ResponseBody
    public List<UserDTO> getMastersByProcedure(@RequestParam Long procedureId) {
        System.out.println("Запрошена процедура по ID : " + procedureId);
        Procedure procedure = procedureRepository.findById(procedureId).orElse(null);

        if (procedure != null) {
            return procedure.getMasters().stream()
                    .map(UserDTO::new)
                    .toList();
        }

        return List.of();
    }

    @PostMapping("/submit-booking")
    public ResponseEntity<String> submitBooking
            (@RequestParam String clientName, @RequestParam String clientSurname,
             @RequestParam String clientPhone, @RequestParam Long procedureId,
             @RequestParam Long masterId, @RequestParam String date,
             @RequestParam String time, @RequestParam(required = false) String email,
             @RequestParam(required = false) String telegramUsername,
             @RequestParam(required = false) Integer preferredMethod,
             Principal principal) {
        User client;
        boolean isNewUser = false;
        String generatedLogin = null;
        String generatedPassword = null;

        if (principal != null) {
            Optional<User> optionalClient = userRepository.findByLogin(principal.getName());
            if (optionalClient.isEmpty()) {
                return ResponseEntity.badRequest().body("Пользователь не найден.");
            }
            client = optionalClient.get();

            UserContact contact = client.getContact();
            if (contact == null) {
                return ResponseEntity.badRequest().body("Контактная информация не найдена.");
            }

            preferredMethod = contact.getPreferredMethod();
            email = contact.getEmail();
            telegramUsername = contact.getTelegramUsername();
        }
        else {
            // Неавторизованный пользователь
            Optional<User> existingUser = userRepository.findByPhoneNum(clientPhone)
                    .filter(u -> u.getName().equalsIgnoreCase(clientName) && u.getSurname().equalsIgnoreCase(clientSurname));

            if (preferredMethod == null) {
                return ResponseEntity.badRequest().body("Не указан способ уведомления.");
            }

            if (existingUser.isPresent()) {
                client = existingUser.get();
            } else {
                isNewUser = true;
                client = new User();
                client.setName(clientName);
                client.setSurname(clientSurname);
                client.setPhoneNum(clientPhone);
                client.setRole(roleRepository.findByName("client"));

                generatedLogin = userService.generateLogin(clientSurname);
                generatedPassword = userService.generateRandomPassword(6);
                client.setLogin(generatedLogin);
                client.setPassword(passwordEncoder.encode(generatedPassword));

                // Создание и установка UserContact
                UserContact contact = new UserContact();
                contact.setPreferredMethod(preferredMethod);
                contact.setEmail(email);
                contact.setTelegramUsername(telegramUsername);
                contact.setUser(client);
                contact.setCreatedAt(LocalDateTime.now());
                contact.setUpdatedAt(LocalDateTime.now());
                client.setContact(contact);

                userRepository.save(client);
            }
        }

        Optional<User> masterOptional = userRepository.findById(masterId);
        if (masterOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Мастер не найден.");
        }
        User master = masterOptional.get();

        Optional<Procedure> procedureOptional = procedureRepository.findById(procedureId);
        if (procedureOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Процедура не найдена.");
        }
        Procedure procedure = procedureOptional.get();

        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);

        Request request = new Request();
        request.setClient(client);
        request.setMaster(master);
        request.setProcedure(procedure);
        request.setDate(localDate);
        request.setTime(localTime);
        request.setStatus("AWAIT");
        requestRepository.save(request);

        Integer requestId = request.getId();

        // Блокировка слотов
        int durationMinutes = procedure.getExTime().toSecondOfDay() / 60;
        int slotsToBlock = durationMinutes / 30;
        for (int i = 0; i < slotsToBlock; i++) {
            LocalTime slotTime = localTime.plusMinutes(i * 30);
            timetableRepository.findByUserIdAndDateAndTime(masterId, localDate, slotTime)
                    .ifPresent(slot -> {
                        slot.setStatus("TAKEN");
                        slot.setRequestId(requestId);
                        timetableRepository.save(slot);
                    });
        }

        // Отправка уведомления по email
        UserContact contact = client.getContact();
        if (contact != null && contact.getPreferredMethod() == 1 &&
                contact.getEmail() != null && !contact.getEmail().isBlank()) {
            try {
                Locale russian = new Locale("ru");
                String formattedDate = request.getDate().getDayOfMonth() + " " +
                        request.getDate().getMonth().getDisplayName(TextStyle.FULL, russian);

                if (isNewUser) {
                    emailService.sendBookingConfirmationAndRegistrationEmail(
                            contact.getEmail(),
                            client.getName(),
                            formattedDate,
                            request.getTime(),
                            request.getProcedure().getName(),
                            request.getMaster().getName(),
                            request.getProcedure().getPrice().toString(),
                            generatedLogin,
                            generatedPassword
                    );
                } else {
                    emailService.sendBookingConfirmationEmail(
                            contact.getEmail(),
                            client.getName(),
                            formattedDate,
                            request.getTime(),
                            request.getProcedure().getName(),
                            request.getMaster().getName(),
                            request.getProcedure().getPrice().toString()
                    );
                }
            } catch (Exception e) {
                logger.error("Ошибка при отправке письма подтверждения: {}", e.getMessage());
            }
        }

        return ResponseEntity.ok("Запись успешно создана.");
    }


}
