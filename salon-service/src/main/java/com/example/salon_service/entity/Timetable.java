package com.example.salon_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "timetable")
@Data
public class Timetable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "date", nullable = false)
    @JsonProperty("date")
    private LocalDate date;

    @Column(name = "time", nullable = false)
    @JsonProperty("time")
    private LocalTime time;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "m_id", nullable = false)
    @JsonProperty("user")
    @JsonIgnore
    private User user;

    @Column(name = "status", nullable = false, length = 10)
    @JsonProperty("status")
    private String status;

    @Column(name = "request_id")
    private Integer requestId; // связанная со слотом запись

}
