package com.example.salon_service.controller;

import com.example.salon_service.entity.*;
import com.example.salon_service.mail.EmailService;
import com.example.salon_service.repository.ProcedureRepository;
import com.example.salon_service.repository.RoleRepository;
import com.example.salon_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @Autowired private final UserRepository userRepository;
    @Autowired private final RoleRepository roleRepository;
    @Autowired private final ProcedureRepository procedureRepository;
    @Autowired private final PasswordEncoder passwordEncoder;
    @Autowired private final EmailService emailService;

    public RegistrationController(UserRepository userRepository,
                                  RoleRepository roleRepository,
                                  ProcedureRepository procedureRepository,
                                  PasswordEncoder passwordEncoder,
                                  EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.procedureRepository = procedureRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "public/register";
    }

    @GetMapping("/register-employee")
    public String showEmployeeRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("procedures", procedureRepository.findAll());
        return "admin/register-employee";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String name,
                               @RequestParam String surname,
                               @RequestParam String phoneNum,
                               @RequestParam String login,
                               @RequestParam String password,
                               @RequestParam Integer preferredMethod,
                               @RequestParam(required = false) String email,
                               @RequestParam(required = false) String telegramUsername,
                               Model model) {

        if (userRepository.findByLogin(login).isPresent()) {
            model.addAttribute("error", "Логин уже занят");
            return "public/register";
        }

        Role clientRole = roleRepository.findByName("client");
        if (clientRole == null) {
            model.addAttribute("error", "Роль 'client' не найдена в базе");
            return "public/register";
        }

        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setPhoneNum(phoneNum);
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(clientRole);

        UserContact contact = new UserContact();
        contact.setPreferredMethod(preferredMethod);
        contact.setEmail(email);
        contact.setTelegramUsername(telegramUsername);
        contact.setUser(user);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        user.setContact(contact);

        userRepository.save(user);

        // Отправка письма, если выбран способ "email" и email указан
        if (preferredMethod == 1 && email != null && !email.isBlank()) {
            emailService.sendRegistrationEmail(email, name, login);
        }

        return "redirect:/login?registered=true";
    }

    @Transactional
    @PostMapping("/register-employee")
    public String registerEmployee(@RequestBody EmployeeRegisterDTO emplRegisterDTO,
                                   BindingResult bindingResult,
                                   @RequestParam("procedureIds") List<Long> procedureIds,
                                   @RequestParam Integer preferredMethod,
                                   @RequestParam(required = false) String email,
                                   @RequestParam(required = false) String telegramUsername,
                                   Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("procedures", procedureRepository.findAll());
            return "redirect:/lkemployee?error=validation";
        }

        if (userRepository.findByLogin(emplRegisterDTO.getLogin()).isPresent()) {
            model.addAttribute("error", "Логин уже занят");
            return "redirect:/lkemployee?error=login_taken";
        }

        if (emplRegisterDTO.getPassword() == null || emplRegisterDTO.getPassword().isEmpty()) {
            model.addAttribute("error", "Пароль не может быть пустым");
            return "redirect:/lkemployee?error=password_empty";
        }

        Role masterRole = roleRepository.findByName("master");
        if (masterRole == null) {
            model.addAttribute("error", "Роль 'master' не найдена в базе");
            return "redirect:/lkemployee?error=role_not_found";
        }

        User user = new User();
        user.setName(emplRegisterDTO.getName());
        user.setSurname(emplRegisterDTO.getSurname());
        user.setPhoneNum(emplRegisterDTO.getPhoneNum());
        user.setLogin(emplRegisterDTO.getLogin());
        user.setPassword(passwordEncoder.encode(emplRegisterDTO.getPassword()));
        user.setRole(masterRole);

        List<Procedure> selectedProcedures = procedureRepository.findAllById(procedureIds);
        if (selectedProcedures.isEmpty()) {
            model.addAttribute("error", "Не выбраны услуги");
            return "redirect:/lkemployee?error=no_procedures";
        }

        user.setProcedures(selectedProcedures);

        // Контакт
        UserContact contact = new UserContact();
        contact.setPreferredMethod(preferredMethod);
        contact.setEmail(email);
        contact.setTelegramUsername(telegramUsername);
        contact.setUser(user);
        contact.setCreatedAt(LocalDateTime.now());
        contact.setUpdatedAt(LocalDateTime.now());

        user.setContact(contact);

        userRepository.save(user);
        model.addAttribute("success", "Сотрудник успешно зарегистрирован");
        return "redirect:/lkemployee";
    }
}
