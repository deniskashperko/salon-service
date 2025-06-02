package com.example.salon_service.config;

import com.example.salon_service.entity.User;
import com.example.salon_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PasswordHashInitializer.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Флаг, указывающий, что хеширование уже было выполнено
    private static boolean alreadyHashed = false;

    public PasswordHashInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (alreadyHashed) {
            logger.info("Пароли уже были захешированы ранее. Пропуск...");
            return;
        }

        boolean anyUpdated = false;
        for (User user : userRepository.findAll()) {
            String plainPassword = user.getPassword();
            if (!plainPassword.startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(plainPassword));
                userRepository.save(user);
                logger.info("Хеширован пароль для пользователя: {}", user.getLogin());
                anyUpdated = true;
            }
        }
        if (anyUpdated) {
            alreadyHashed = true;
            logger.info("Все необходимые пароли были успешно захешированы.");
        } else {
            logger.info("Все пароли уже были захешированы ранее.");
        }
    }
}
