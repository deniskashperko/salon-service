package com.example.salon_service.controller;

import com.example.salon_service.entity.Procedure;
import com.example.salon_service.entity.User;
import com.example.salon_service.repository.ProcedureRepository;
import com.example.salon_service.repository.UserRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {

    private final UserRepository userRepository;
    private final ProcedureRepository procedureRepository;

    public PageController(UserRepository userRepository, ProcedureRepository procedureRepository) {
        this.userRepository = userRepository;
        this.procedureRepository = procedureRepository;
    }

    // Главная страница с данными для модального окна
    @GetMapping({"/", "/index"})
    public String showIndexPage(Model model) {
        List<User> masters = userRepository.findByRoleName("master");
        List<Procedure> procedures = procedureRepository.findAll();

        model.addAttribute("masters", masters);
        model.addAttribute("procedures", procedures);

        return "public/index";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/contacts")
    public String contacts() {
        return "public/contacts";
    }

    @GetMapping("/procedures-list")
    public String proceduresList() {
        return "public/procedures-list";
    }

    @GetMapping("/recommendations")
    public String recommendations() {
        return "public/recommendations";
    }

    @GetMapping("/reviews")
    public String reviews() {
        return "public/reviews";
    }

    @GetMapping("/login")
    public String login() {
        return "public/login";
    }
}
