package com.example.salon_service.repository;

import com.example.salon_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    Optional<User> findByPhoneNum(String phone);
    Optional<User> findByContact_Email(String email);

    @Query("SELECT u FROM User u WHERE u.role.name = :name")
    List<User> findByRoleName(@Param("name") String name);

    @Modifying
    @Query(value = "DELETE FROM p_list WHERE m_id = :masterId AND p_id = :procedureId", nativeQuery = true)
    void deleteProcedureFromMaster(@Param("masterId") Long masterId,
                                   @Param("procedureId") Long procedureId);

}
