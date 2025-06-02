package com.example.salon_service.service;

import com.example.salon_service.entity.User;
import com.example.salon_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void updateUser(User user) {
        // Логика обновления данных пользователя
        userRepository.save(user); // Сохранение обновленных данных в базу
    }

    // Проверка, совпадает ли сырой (введённый) пароль с зашифрованным
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    // Обновление пароля
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword)); // Шифруем новый пароль
        userRepository.save(user);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void removeProcedureFromMaster(Long masterId, Long procedureId) {
        userRepository.deleteProcedureFromMaster(masterId, procedureId);
    }

    public List<User> findAllClients() {
        return userRepository.findByRoleName("client");
    }
    public List<User> findAllEmployees() {
        return userRepository.findByRoleName("master");
    }

    public void deleteEmployee(Long employeeId) {
        // Проверка на существование сотрудника в базе данных
        Optional<User> employeeOptional = userRepository.findById(employeeId);
        if (employeeOptional.isPresent()) {
            User employee = employeeOptional.get();
            // Удаление сотрудника
            userRepository.delete(employee);
        } else {
            throw new RuntimeException("Сотрудник не найден");
        }
    }

    // Метод для генерации логина (Фамилия + случайные цифры)
    public String generateLogin(String surname) {
        Random random = new Random();
        int number = random.nextInt(1000, 9999);  // Генерация 4-значного числа
        return surname.toLowerCase() + number;
    }

    // Метод для генерации случайного пароля
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }


}
