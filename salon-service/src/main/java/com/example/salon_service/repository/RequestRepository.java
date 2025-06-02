package com.example.salon_service.repository;

import com.example.salon_service.entity.Request;
import com.example.salon_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

    @Query("SELECT r FROM Request r WHERE r.client.id = :clientId " +
            "ORDER BY " +
            "CASE r.status " +
            "   WHEN 'AWAIT' THEN 0 " +
            "   WHEN 'DONE' THEN 1 " +
            "   WHEN 'CANCELLED' THEN 2 " +
            "   ELSE 3 END, " +
            "r.date ASC, r.time ASC")
    List<Request> findSortedByClientId(@Param("clientId") Long clientId);

    @Query("SELECT r FROM Request r WHERE r.master.id = :masterId " +
            "ORDER BY " +
            "CASE r.status " +
            "   WHEN 'AWAIT' THEN 0 " +
            "   WHEN 'DONE' THEN 1 " +
            "   WHEN 'CANCELLED' THEN 2 " +
            "   ELSE 3 END, " +
            "r.date ASC, r.time ASC")
    List<Request> findSortedByMasterId(@Param("masterId") Long masterId);

    // Общее количество заявок по мастеру в диапазоне дат
    @Query("SELECT COUNT(r) FROM Request r WHERE r.master.id = :masterId AND " +
            "(r.date BETWEEN :startDate AND :endDate OR (r.status = 'AWAIT' AND r.date > :endDate))")
    long countByMasterIdInDateRange(@Param("masterId") Long masterId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    // Общее количество заявок по всем мастерам в диапазоне дат
    @Query("SELECT COUNT(r) FROM Request r WHERE " +
            "(r.date BETWEEN :startDate AND :endDate OR (r.status = 'AWAIT' AND r.date > :endDate))")
    long countAllInDateRange(@Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    // Количество заявок по статусу в диапазоне дат
    @Query("SELECT COUNT(r) FROM Request r WHERE r.master.id = :masterId " +
            "AND r.status = :status AND (r.date BETWEEN :startDate AND :endDate OR (r.status = 'AWAIT' AND r.date > :endDate))")
    long countByMasterIdAndStatusInDateRange(@Param("masterId") Long masterId,
                                             @Param("status") String status,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // Количество заявок по статусу по всем мастерам в диапазоне дат
    @Query("SELECT COUNT(r) FROM Request r WHERE r.status = :status AND " +
            "(r.date BETWEEN :startDate AND :endDate OR (r.status = 'AWAIT' AND r.date > :endDate))")
    long countByStatusInDateRange(@Param("status") String status,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    // Группировка по процедурам в диапазоне дат для мастера
    @Query("SELECT r.procedure.name, COUNT(r) FROM Request r WHERE r.master.id = :masterId AND r.date BETWEEN :startDate AND :endDate GROUP BY r.procedure.name ORDER BY COUNT(r) DESC")
    List<Object[]> countRequestsByProcedureInDateRange(@Param("masterId") Long masterId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Группировка по процедурам в диапазоне дат для всех мастеров
    @Query("SELECT r.procedure.name, COUNT(r) FROM Request r WHERE r.date BETWEEN :startDate AND :endDate GROUP BY r.procedure.name ORDER BY COUNT(r) DESC")
    List<Object[]> countAllRequestsByProcedureInDateRange(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    // Сумма цен за выполненные заявки для мастера
    @Query("SELECT SUM(r.procedure.price) FROM Request r WHERE r.master.id = :masterId AND r.status = 'DONE' AND r.date BETWEEN :startDate AND :endDate")
    BigDecimal sumPriceByMasterIdInDateRange(@Param("masterId") Long masterId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // Сумма цен за выполненные заявки для всех мастеров
    @Query("SELECT SUM(r.procedure.price) FROM Request r WHERE r.status = 'DONE' AND r.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAllPricesInDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Количество уникальных клиентов для мастера
    @Query("SELECT COUNT(DISTINCT r.client.id) FROM Request r WHERE r.master.id = :masterId AND r.date BETWEEN :startDate AND :endDate")
    Long countDistinctClientsByMasterIdInDateRange(@Param("masterId") Long masterId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Количество уникальных клиентов для всех мастеров
    @Query("SELECT COUNT(DISTINCT r.client.id) FROM Request r WHERE r.date BETWEEN :startDate AND :endDate")
    Long countAllDistinctClientsInDateRange(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // Средняя цена услуг для мастера
    @Query("SELECT AVG(p.price) FROM Request r JOIN r.procedure p WHERE r.master.id = :masterId AND r.date BETWEEN :startDate AND :endDate")
    Double findAverageServicePriceInDateRange(@Param("masterId") Long masterId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Средняя цена услуг для всех мастеров
    @Query("SELECT AVG(p.price) FROM Request r JOIN r.procedure p WHERE r.date BETWEEN :startDate AND :endDate")
    Double findAverageServicePriceAllInDateRange(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    // Топ-3 популярных услуги для мастера
    @Query("SELECT p.name, COUNT(r) FROM Request r JOIN r.procedure p WHERE r.master.id = :masterId AND r.date BETWEEN :startDate AND :endDate GROUP BY p.name ORDER BY COUNT(r) DESC")
    List<Object[]> findTop3PopularServicesInDateRange(@Param("masterId") Long masterId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    // Топ-3 популярных услуги для всех мастеров
    @Query("SELECT p.name, COUNT(r) FROM Request r JOIN r.procedure p WHERE r.date BETWEEN :startDate AND :endDate GROUP BY p.name ORDER BY COUNT(r) DESC")
    List<Object[]> findTop3PopularServicesAllInDateRange(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    // Наименее популярные услуги для мастера
    @Query("SELECT p.name, COUNT(r) FROM Request r JOIN r.procedure p WHERE r.master.id = :masterId AND r.date BETWEEN :startDate AND :endDate GROUP BY p.name ORDER BY COUNT(r) ASC")
    List<Object[]> findLeastPopularServicesInDateRange(@Param("masterId") Long masterId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    // Наименее популярные услуги для всех мастеров
    @Query("SELECT p.name, COUNT(r) FROM Request r JOIN r.procedure p WHERE r.date BETWEEN :startDate AND :endDate GROUP BY p.name ORDER BY COUNT(r) ASC")
    List<Object[]> findLeastPopularServicesAllInDateRange(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    // Повторные клиенты (в диапазоне дат) для мастера
    @Query("SELECT r.client, COUNT(r) FROM Request r WHERE r.master.id = :masterId AND r.date BETWEEN :startDate AND :endDate GROUP BY r.client HAVING COUNT(r) >= 3")
    List<Object[]> findRepeatClientsInDateRange(@Param("masterId") Long masterId,
                                                @Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // Повторные клиенты (в диапазоне дат) для всех мастеров
    @Query("SELECT r.client, COUNT(r) FROM Request r WHERE r.date BETWEEN :startDate AND :endDate GROUP BY r.client HAVING COUNT(r) >= 3")
    List<Object[]> findAllRepeatClientsInDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    List<Request> findAllByStatusAndDate(String await, LocalDate tomorrow);
}
