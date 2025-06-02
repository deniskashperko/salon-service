package com.example.salon_service.repository;

import com.example.salon_service.entity.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
    Optional<UserContact> findByTelegramUsername(String username);

    Optional<UserContact> findByUser_Id(Long id);
}
