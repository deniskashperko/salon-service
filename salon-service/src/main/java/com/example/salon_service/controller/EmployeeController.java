package com.example.salon_service.controller;

import com.example.salon_service.entity.*;
import com.example.salon_service.repository.*;
import com.example.salon_service.service.*;
import com.example.salon_service.config.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Controller
public class EmployeeController {

    @Autowired private UserRepository userRepository;
    @Autowired private UserContactRepository contactRepository;
    @Autowired private UserService userService;
    @Autowired private RequestService requestService;
    @Autowired private RequestRepository requestRepository;
    @Autowired private ProcedureService procedureService;
    @Autowired private TimetableRepository timetableRepository;
    @Autowired private ProcedureRepository procedureRepository;

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Сотрудник не найден"));
    }

    @GetMapping("/lkemployee")
    public String showEmployeePage(Model model, Authentication authentication, Principal principal) {
        // Получаем данные пользователя
        User user = getUserByPrincipal(principal);
        model.addAttribute("user", user);

        // Проверяем роль пользователя и добавляем соответствующие атрибуты в модель
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            List<User> employees = userService.findAllEmployees(); // Получаем всех сотрудников
            Optional<UserContact> contacts = contactRepository.findByUser_Id(user.getId());
            model.addAttribute("isAdmin", true);
            model.addAttribute("employees", employees); // Отображаем список сотрудников
            model.addAttribute("procedures", procedureRepository.findAll());
            model.addAttribute("contacts", contacts);
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("master"))) {
            List<Procedure> allProcedures = procedureService.getAllProcedures();
            List<Procedure> unavailableProcedures = user.getUnavailableProcedures(allProcedures);
            Optional<UserContact> contacts = contactRepository.findByUser_Id(user.getId());
            model.addAttribute("isMaster", true);
            model.addAttribute("procedures", user.getProcedures());  // Отображаем услуги мастера
            model.addAttribute("unavailableProcedures", unavailableProcedures);  // Отображаем недоступные услуги мастера
            model.addAttribute("contacts", contacts);
        }
        return "lkemp/lkemployee";
    }

    @GetMapping("/lkemployee/records")
    public String getRecordsPage(Model model, Principal principal, Authentication authentication) {
        User user = getUserByPrincipal(principal);
        model.addAttribute("user", user);

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("master"))) {
            List<Request> masterRequests = requestService.getRequestsForMaster(user.getId());
            model.addAttribute("isMaster", true);
            model.addAttribute("masterRequests", masterRequests);
            LocalDate today = LocalDate.now();

            // Списки по категориям
            List<Request> todayRequests = masterRequests.stream()
                    .filter(r -> "AWAIT".equals(r.getStatus()) && today.equals(r.getDate()))
                    .collect(Collectors.toList());

            List<Request> futureRequests = masterRequests.stream()
                    .filter(r -> "AWAIT".equals(r.getStatus()) && r.getDate().isAfter(today))
                    .collect(Collectors.toList());

            List<Request> pastRequests = masterRequests.stream()
                    .filter(r -> !"AWAIT".equals(r.getStatus()) || r.getDate().isBefore(today))
                    .collect(Collectors.toList());

            model.addAttribute("todayRequests", todayRequests);
            model.addAttribute("futureRequests", futureRequests);
            model.addAttribute("pastRequests", pastRequests);
        }
        else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            // Получаем все записи для админа
            List<Request> allRequests = requestRepository.findAll(Sort.by(Sort.Order.asc("date")));
            model.addAttribute("isAdmin", true);
            model.addAttribute("allRequests", allRequests);

            LocalDate today = LocalDate.now();

            // Списки по категориям для админа
            List<Request> todayRequests = allRequests.stream()
                    .filter(r -> "AWAIT".equals(r.getStatus()) && today.equals(r.getDate()))
                    .collect(Collectors.toList());

            List<Request> futureRequests = allRequests.stream()
                    .filter(r -> "AWAIT".equals(r.getStatus()) && r.getDate().isAfter(today))
                    .collect(Collectors.toList());

            List<Request> pastRequests = allRequests.stream()
                    .filter(r -> !"AWAIT".equals(r.getStatus()) || r.getDate().isBefore(today))
                    .collect(Collectors.toList());

            model.addAttribute("todayRequests", todayRequests);
            model.addAttribute("futureRequests", futureRequests);
            model.addAttribute("pastRequests", pastRequests);
        }
        return "lkemp/lkemp-records";
    }

    @GetMapping("/lkemployee/clients")
    public String getClientsPage(Model model, Principal principal, Authentication authentication) {
        User user = getUserByPrincipal(principal);
        model.addAttribute("user", user);
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            model.addAttribute("isAdmin", true);
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("master"))) {
            model.addAttribute("isMaster", true);
        }
        // Получаем всех клиентов
        List<User> clients = userService.findAllClients()
                .stream()
                .sorted(Comparator.comparing(User::getName)) // сортировка по имени
                .collect(Collectors.toList());
        model.addAttribute("users", clients);
        Map<Long, UserContact> contactMap = contactRepository.findAll().stream()
                .filter(c -> c.getUser() != null)
                .collect(Collectors.toMap(c -> c.getUser().getId(), c -> c));
        model.addAttribute("contactMap", contactMap);


        return "lkemp/lkemp-clients";
    }

    @GetMapping("/get-client-records/{clientId}")
    public ResponseEntity<List<RequestDTO>> getClientRecords(@PathVariable Long clientId) {
        List<Request> clientRequests = requestService.getRequestsForClient(clientId);
        if (clientRequests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(List.of());
        }

        List<RequestDTO> dtoList = clientRequests.stream()
                .map(RequestDTO::new)
                .toList();

        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/lkemployee/procedures")
    public String getProceduresPage(Model model, Principal principal, Authentication authentication) {
        User user = getUserByPrincipal(principal);
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            model.addAttribute("isAdmin", true);
            model.addAttribute("procedures", procedureService.getAllProcedures());  // Отображаем услуги мастера
        }
        return "lkemp/lkemp-procedures";
    }

    @GetMapping("/lkemployee/schedule")
    public String getSchedulePage(Model model, Principal principal, Authentication authentication) {
        User user = getUserByPrincipal(principal);
        model.addAttribute("user", user);
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            List<User> employees = userService.findAllEmployees();
            model.addAttribute("employees", employees);
            model.addAttribute("isAdmin", true);
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("master"))) {
            model.addAttribute("isMaster", true);
            model.addAttribute("masterId", user.getId());
        }
        return "lkemp/lkemp-schedule";
    }

    @GetMapping("/get-schedule-dates")
    public ResponseEntity<List<LocalDate>> getAllDatesForMaster(@RequestParam Long masterId) {
        List<LocalDate> allDates = timetableRepository.findAllDatesByMasterId(masterId);
        return ResponseEntity.ok(allDates);
    }

    @GetMapping("/get-schedule-slots")
    @ResponseBody
    public ResponseEntity<List<Timetable>> getAllSlotsForMaster(
            @RequestParam Long masterId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<Timetable> slots = timetableRepository.findByUserIdAndDate(masterId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/block-slots")
    public ResponseEntity<?> blockSlots(@RequestBody Map<String, List<Integer>> payload) {
        List<Integer> slotIds = payload.get("slotIds");
        timetableRepository.findAllById(slotIds).forEach(slot -> {
            if ("FREE".equals(slot.getStatus())) {
                slot.setStatus("TAKEN");
            }
        });
        timetableRepository.flush(); // или saveAll
        return ResponseEntity.ok().build();
    }

    @GetMapping("/lkemployee/statistics")
    public String getStatisticsPage(Model model, Principal principal, Authentication authentication) {
        User user = getUserByPrincipal(principal);
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
            List<User> employees = userService.findAllEmployees();
            model.addAttribute("employees", employees);
            model.addAttribute("isAdmin", true);
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("master"))) {
            model.addAttribute("isMaster", true);
            model.addAttribute("masterId", user.getId());
        }
        return "lkemp/lkemp-statistics";
    }

    @PostMapping("/update-employee")
    public String updateProfile(@ModelAttribute("user") User updatedData,
                                @RequestParam("email") String email,Principal principal) {
        User currentUser = getUserByPrincipal(principal);
        currentUser.setName(updatedData.getName());
        currentUser.setSurname(updatedData.getSurname());
        currentUser.setPhoneNum(updatedData.getPhoneNum());
        UserContact contact = currentUser.getContact();
        if (contact == null) {
            contact = new UserContact();
            contact.setUser(currentUser);
            contact.setPreferredMethod(1);
            contact.setCreatedAt(now());
        }
        contact.setEmail(email);
        contact.setUpdatedAt(now());
        currentUser.setContact(contact);

        userService.updateUser(currentUser);
        return "redirect:/lkemployee";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> changePassword(@RequestParam("oldPassword") String oldPassword,
                                                              @RequestParam("newPassword") String newPassword,
                                                              @RequestParam("confirmPassword") String confirmPassword,
                                                              Principal principal) {
        User user = getUserByPrincipal(principal);
        // Проверка, что новый пароль и подтверждение совпадают
        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Новый пароль и подтверждение не совпадают."));
        }
        // Проверка, что старый пароль введен верно
        if (!userService.checkPassword(user, oldPassword)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Неверный старый пароль."));
        }
        // Обновление пароля
        userService.updatePassword(user, newPassword);
        return ResponseEntity.ok(Map.of("success", "Пароль успешно изменен."));
    }

    @PostMapping("/add-procedure-for-master")
    public ResponseEntity<String> addProcedureToMaster(@AuthenticationPrincipal UserDetails userDetails,
                                                       @RequestBody Map<String, Long> payload) {
        Long procedureId = payload.get("procedureId");
        User master = userService.findByLogin(userDetails.getUsername());
        Procedure procedure = procedureService.getProcedureById(procedureId);

        if (master != null && procedure != null) {
            master.getProcedures().add(procedure);
            userService.save(master); // сохраним связь через JPA
            return ResponseEntity.ok("Услуга добавлена успешно.");
        } else {
            return ResponseEntity.badRequest().body("Ошибка при добавлении услуги.");
        }
    }

    @PostMapping("/delete-procedure-for-master")
    public ResponseEntity<String> deleteProcedureForMaster(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestParam Long procedureId) {
        User master = userService.findByLogin(userDetails.getUsername());
        if (master != null && procedureService.getProcedureById(procedureId) != null) {
            userService.removeProcedureFromMaster(master.getId(), procedureId);
            return ResponseEntity.ok("Услуга удалена успешно.");
        } else {
            return ResponseEntity.badRequest().body("Ошибка при удалении услуги.");
        }
    }

}
