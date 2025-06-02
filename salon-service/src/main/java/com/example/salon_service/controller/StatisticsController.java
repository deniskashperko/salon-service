package com.example.salon_service.controller;

import com.example.salon_service.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final RequestRepository requestRepository;

    @GetMapping("/requests")
    public ResponseEntity<Map<String, Long>> getRequestStatistics(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        long total = masterId != null
                ? requestRepository.countByMasterIdInDateRange(masterId, startDate, endDate)
                : requestRepository.countAllInDateRange(startDate, endDate);

        long active = masterId != null
                ? requestRepository.countByMasterIdAndStatusInDateRange(masterId, "AWAIT", startDate, endDate)
                : requestRepository.countByStatusInDateRange("AWAIT", startDate, endDate);

        long completed = masterId != null
                ? requestRepository.countByMasterIdAndStatusInDateRange(masterId, "DONE", startDate, endDate)
                : requestRepository.countByStatusInDateRange("DONE", startDate, endDate);

        long cancelled = masterId != null
                ? requestRepository.countByMasterIdAndStatusInDateRange(masterId, "CANCELLED", startDate, endDate)
                : requestRepository.countByStatusInDateRange("CANCELLED", startDate, endDate);

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/requests-by-procedure")
    public ResponseEntity<Map<String, Long>> getRequestsByProcedure(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Object[]> results = masterId != null
                ? requestRepository.countRequestsByProcedureInDateRange(masterId, startDate, endDate)
                : requestRepository.countAllRequestsByProcedureInDateRange(startDate, endDate);

        Map<String, Long> procedureStats = new LinkedHashMap<>();
        for (Object[] row : results) {
            procedureStats.put((String) row[0], (Long) row[1]);
        }
        return ResponseEntity.ok(procedureStats);
    }

    @GetMapping("/revenue-and-clients")
    public ResponseEntity<Map<String, Object>> getRevenueAndClients(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        BigDecimal revenue = masterId != null
                ? requestRepository.sumPriceByMasterIdInDateRange(masterId, startDate, endDate)
                : requestRepository.sumAllPricesInDateRange(startDate, endDate);

        Long uniqueClients = masterId != null
                ? requestRepository.countDistinctClientsByMasterIdInDateRange(masterId, startDate, endDate)
                : requestRepository.countAllDistinctClientsInDateRange(startDate, endDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
        stats.put("uniqueClients", uniqueClients != null ? uniqueClients : 0);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/average-service-price")
    public ResponseEntity<Double> getAverageServicePrice(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        Double averagePrice = masterId != null
                ? requestRepository.findAverageServicePriceInDateRange(masterId, startDate, endDate)
                : requestRepository.findAverageServicePriceAllInDateRange(startDate, endDate);

        return ResponseEntity.ok(averagePrice != null ? averagePrice : 0.0);
    }

    @GetMapping("/top-popular-services")
    public ResponseEntity<List<Map<String, Object>>> getTop3PopularServices(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Object[]> results = masterId != null
                ? requestRepository.findTop3PopularServicesInDateRange(masterId, startDate, endDate)
                : requestRepository.findTop3PopularServicesAllInDateRange(startDate, endDate);

        List<Map<String, Object>> top3Services = results.stream().map(row -> {
            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put("name", row[0]);
            serviceMap.put("count", row[1]);
            return serviceMap;
        }).toList();

        return ResponseEntity.ok(top3Services);
    }

    @GetMapping("/least-popular-services")
    public ResponseEntity<List<Map<String, Object>>> getLeastPopularServices(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Object[]> results = masterId != null
                ? requestRepository.findLeastPopularServicesInDateRange(masterId, startDate, endDate)
                : requestRepository.findLeastPopularServicesAllInDateRange(startDate, endDate);

        List<Map<String, Object>> leastPopularServices = results.stream().map(row -> {
            Map<String, Object> serviceMap = new HashMap<>();
            serviceMap.put("name", row[0]);
            serviceMap.put("count", row[1]);
            return serviceMap;
        }).toList();

        return ResponseEntity.ok(leastPopularServices);
    }

    @GetMapping("/repeat-clients")
    public ResponseEntity<List<Map<String, Object>>> getRepeatClients(
            @RequestParam(value = "masterId", required = false) Long masterId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<Object[]> results = masterId != null
                ? requestRepository.findRepeatClientsInDateRange(masterId, startDate, endDate)
                : requestRepository.findAllRepeatClientsInDateRange(startDate, endDate);

        List<Map<String, Object>> repeatClients = results.stream().map(row -> {
            Map<String, Object> clientMap = new HashMap<>();
            clientMap.put("client", row[0]);
            clientMap.put("count", row[1]);
            return clientMap;
        }).toList();

        return ResponseEntity.ok(repeatClients);
    }
}
