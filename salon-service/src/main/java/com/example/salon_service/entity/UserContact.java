package com.example.salon_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_contact")
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "preferred_method", nullable = false)
    private Integer preferredMethod; // 1 = Email, 2 = Telegram

    @Column(name = "email")
    private String email;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
