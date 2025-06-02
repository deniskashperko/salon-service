package com.example.salon_service.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EmployeeRegisterDTO {
    private String name;
    private String surname;
    private String phoneNum;
    private String login;
    private String password;
    private List<Long> procedureIds;
    private Integer preferredMethod;         // 1 = Email, 2 = Telegram
    private String email;                    // Email (если выбран Email)
    private String telegramUsername;         // Username Telegram (если выбран Telegram)
}
