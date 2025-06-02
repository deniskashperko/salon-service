package com.example.salon_service.controller;

import com.example.salon_service.entity.Procedure;
import com.example.salon_service.entity.Request;
import com.example.salon_service.entity.User;
import com.example.salon_service.entity.UserContact;
import com.example.salon_service.repository.ProcedureRepository;
import com.example.salon_service.repository.UserContactRepository;
import com.example.salon_service.service.RequestService;
import com.example.salon_service.service.UserService;
import com.example.salon_service.repository.UserRepository;
import com.example.salon_service.config.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.time.LocalDateTime.now;

@Controller
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private RequestService requestService;
    @Autowired private ProcedureRepository procedureRepository;
    @Autowired private UserContactRepository contactRepository;

    @GetMapping("/lkclient")
    public String lkclient(Model model, Principal principal) {
        User user = getUserByPrincipal(principal);
        List<Request> requests = requestService.getRequestsForClient(user);
        List<User> masters = userRepository.findByRoleName("master");
        List<Procedure> procedures = procedureRepository.findAll();
        List<UserContact> contacts = contactRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("contacts", contacts);
        model.addAttribute("requests", requests);
        model.addAttribute("masters", masters);
        model.addAttribute("procedures", procedures);
        return "lkcl/lkclient";
    }

    @PostMapping("/update-client")
    public String updateProfile(@ModelAttribute("user") User updatedData,
                                @RequestParam("email") String email,
                                Principal principal) {
        User currentUser = getUserByPrincipal(principal);
        currentUser.setName(updatedData.getName());
        currentUser.setSurname(updatedData.getSurname());
        currentUser.setPhoneNum(updatedData.getPhoneNum());

        // Обновление email
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
        return "redirect:/lkclient";
    }

    @PostMapping("/update-feedback")
    public String updateFeedback(@RequestParam("requestId") Integer requestId,
                                 @RequestParam("feedback") String feedback) {
        try {
            // Находим заявку по ID
            Request request = requestService.findById(Long.valueOf(requestId));
            if (request != null) {
                // Обновляем отзыв
                request.setFeedback(feedback);
                requestService.save(request);
            }
        } catch (Exception e) {
            return "Ошибка обновления отзыва: " + e.getMessage();
        }
        return "redirect:/lkclient"; // Перенаправляем обратно на страницу с данными пользователя
    }

    private User getUserByPrincipal(Principal principal) {
        return userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }
}
