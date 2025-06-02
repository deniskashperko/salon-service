package com.example.salon_service.service;

import com.example.salon_service.entity.PasswordResetToken;
import com.example.salon_service.entity.User;
import com.example.salon_service.mail.EmailService;
import com.example.salon_service.repository.PasswordResetTokenRepository;
import com.example.salon_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public boolean createResetToken(String identifier) {
        Optional<User> userOpt = userRepository.findByPhoneNum(identifier)
                .or(() -> userRepository.findByContact_Email(identifier))
                .or(() -> userRepository.findByLogin(identifier));
        if (userOpt.isEmpty()) return false;
        User user = userOpt.get();
        if (user.getContact() == null || user.getContact().getEmail() == null || user.getContact().getEmail().isBlank())
            return false;

        String email = user.getContact().getEmail();
        String name = user.getName();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(expiry);
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:9090/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, name, resetLink);

        return true;
    }


    public Optional<User> resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty() || optionalToken.get().isExpired()) {
            return Optional.empty();
        }

        User user = optionalToken.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(optionalToken.get());

        return Optional.of(user);
    }
}
