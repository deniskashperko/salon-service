package com.example.salon_service.repository;

import com.example.salon_service.entity.Request;
import com.example.salon_service.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TimetableRepository extends JpaRepository<Timetable, Integer> {

    @Query("""
    SELECT DISTINCT t.date FROM Timetable t
    WHERE t.user.id = :masterId AND t.status = 'FREE'
    GROUP BY t.date
    HAVING COUNT(t) >= :requiredSlots
    ORDER BY t.date
    """)
    List<LocalDate> findAvailableDates(@Param("masterId") Long masterId, @Param("requiredSlots") int requiredSlots);

    @Query("""
    SELECT DISTINCT t.date FROM Timetable t
    WHERE t.user.id = :masterId
    GROUP BY t.date
    ORDER BY t.date
    """)
    List<LocalDate> findAllDatesByMasterId(@Param("masterId") Long masterId);

    @Query("SELECT MAX(t.date) FROM Timetable t")
    LocalDate findMaxDate();

    List<Timetable> findByUserIdAndDateAndStatusOrderByTime(Long userId, LocalDate date, String status);
    boolean existsByUserIdAndDate(Long userId, LocalDate date);
    long deleteByDateBefore(LocalDate date);
    List<Timetable> findByUserIdAndDate(Long userId, LocalDate date);
    Optional<Timetable> findByUserIdAndDateAndTime(Long userId, LocalDate date, LocalTime time);
    List<Timetable> findAllByRequestId(Long requestId);

    @Modifying
    @Query("UPDATE Timetable t SET t.status = 'OLD' WHERE t.date < :currentDate OR (t.date = :currentDate AND t.time < :currentTime)")
    int updateStatusToOldForPastSlots(@Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime);



}
