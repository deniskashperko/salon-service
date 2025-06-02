package com.example.salon_service.controller;

import com.example.salon_service.entity.Procedure;
import com.example.salon_service.entity.User;
import com.example.salon_service.repository.ProcedureRepository;
import com.example.salon_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;
import java.util.*;

@Controller
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired private UserRepository userRepository;
    @Autowired private ProcedureRepository procedureRepository;

    @Transactional
    @DeleteMapping("/delete-employee/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long employeeId) {
        Optional<User> userOpt = userRepository.findById(employeeId);
        if (userOpt.isEmpty()) {
            logger.warn("Попытка удалить несуществующего сотрудника с ID: {}", employeeId);
            return ResponseEntity.notFound().build();
        }
        try {
            userRepository.deleteById(employeeId);
            logger.info("Сотрудник с ID {} успешно удалён.", employeeId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении сотрудника с ID {}: {}", employeeId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional
    @DeleteMapping("/delete-client/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long clientId) {
        Optional<User> userOpt = userRepository.findById(clientId);
        if (userOpt.isEmpty()) {
            logger.warn("Попытка удалить несуществующего клиента с ID: {}", clientId);
            return ResponseEntity.notFound().build();
        }
        try {
            userRepository.deleteById(clientId);
            logger.info("Клиент с ID {} успешно удалён.", clientId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении клиента с ID {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/admin/update-client")
    @ResponseBody
    public ResponseEntity<?> updateClient(@RequestBody Map<String, String> payload) {
        try {
            Long id = Long.valueOf(payload.get("id"));
            String login = payload.get("login").trim();
            String phoneNum = payload.get("phoneNum").trim();
            String name = payload.get("name").trim();
            String surname = payload.get("surname").trim();

            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Пользователь не найден");
            }

            User user = optionalUser.get();

            // Проверка на уникальность логина (если изменился)
            if (!user.getLogin().equals(login)) {
                Optional<User> userWithSameLogin = userRepository.findByLogin(login);
                if (userWithSameLogin.isPresent()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Такой логин уже существует");
                }
                user.setLogin(login);
            }
            // Обновляем данные
            if (!user.getName().equals(name)) {
                user.setName(name);
            }
            if (!user.getSurname().equals(surname)) {
                user.setSurname(surname);
            }
            if (!user.getPhoneNum().equals(phoneNum)) {
                user.setPhoneNum(phoneNum);
            }
            userRepository.save(user);
            Map<String, String> response = new HashMap<>();
            response.put("login", user.getLogin());
            response.put("phoneNum", user.getPhoneNum());
            response.put("name", user.getName());
            response.put("surname", user.getSurname());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении клиента: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении данных");
        }
    }

    @Transactional
    @DeleteMapping("/delete-procedure/{procedureId}")
    public ResponseEntity<Void> deleteProcedure(@PathVariable Long procedureId) {
        Optional<Procedure> procedureOpt = procedureRepository.findById(procedureId);

        if (procedureOpt.isEmpty()) {
            logger.warn("Попытка удалить несуществующую услугу с ID: {}", procedureId);
            return ResponseEntity.notFound().build();
        }

        try {
            procedureRepository.deleteById(procedureId);
            logger.info("Услуга с ID {} успешно удалена.", procedureId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при удалении клиента с ID {}: {}", procedureId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add-procedure")
    public String addProcedure(@RequestParam("name") String name, @RequestParam("price") Integer price,
                               @RequestParam("exTimeMinutes") Integer exTimeMinutes) {
        LocalTime exTime = LocalTime.of(exTimeMinutes / 60, exTimeMinutes % 60);
        Procedure procedure = new Procedure();
        procedure.setName(name);
        procedure.setPrice(price);
        procedure.setExTime(exTime);
        procedureRepository.save(procedure);

        return "redirect:/lkemployee/procedures";
    }

    @PostMapping("/admin/update-procedure")
    @ResponseBody
    public ResponseEntity<?> updateProcedure(@RequestBody Map<String, String> payload) {
        try {
            Long id = Long.valueOf(payload.get("id"));
            String name = payload.get("name").trim();
            Integer price = Integer.valueOf(payload.get("price").trim());
            String exTimeStr = payload.get("exTime").trim();
            LocalTime exTime = LocalTime.parse(exTimeStr);
            Optional<Procedure> optProcedure = procedureRepository.findById(id);
            if (optProcedure.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Услуга не найдена");
            }
            Procedure procedure = optProcedure.get();
            boolean updated = false;
            if (!procedure.getName().equals(name)) {
                procedure.setName(name);
                updated = true;
            }
            if (!procedure.getPrice().equals(price)) {
                procedure.setPrice(price);
                updated = true;
            }
            if (!procedure.getExTime().equals(exTime)) {
                procedure.setExTime(exTime);
                updated = true;
            }
            if (updated) {
                procedureRepository.save(procedure);
            }
            Map<String, String> response = new HashMap<>();
            response.put("name", procedure.getName());
            response.put("price", String.valueOf(procedure.getPrice()));
            response.put("exTime", procedure.getExTime().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении услуги: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при обновлении данных");
        }
    }

}
