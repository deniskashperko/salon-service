package com.example.salon_service.controller;

import com.example.salon_service.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.salon_service.mail.EmailService;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final EmailService emailService;

    @GetMapping("/forgot-password")
    public String showForgotForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgot(@RequestParam("identifier") String identifier) {
        boolean success = passwordResetService.createResetToken(identifier);

        if (!success) {
            return "redirect:/forgot-password?notfound=true";
        }

        return "redirect:/forgot-password?sent=true";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleReset(@RequestParam String token,
                              @RequestParam String password,
                              @RequestParam String confirm,
                              RedirectAttributes redirectAttributes) {
        if (!password.equals(confirm)) {
            redirectAttributes.addFlashAttribute("error", "Пароли не совпадают.");
            return "redirect:/reset-password?token=" + token;
        }

        var optionalUser = passwordResetService.resetPassword(token, password);
        if (optionalUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ссылка недействительна или истекла.");
            return "redirect:/forgot-password";
        }

        // Отправка письма об успешной смене пароля
        var user = optionalUser.get();
        if (user.getContact().getEmail() != null && !user.getContact().getEmail().isBlank()) {
            emailService.sendPasswordChangedEmail(user.getContact().getEmail(), user.getName());
        }

        return "redirect:/login?success=true";
    }
}
