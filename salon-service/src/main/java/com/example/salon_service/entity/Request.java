package com.example.salon_service.entity;

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
@Table(name = "requests")
@Data
public class Request {

    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "requests_seq")
    @SequenceGenerator(name = "requests_seq", sequenceName = "requests_id_seq", allocationSize = 1)
    @JsonProperty("id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "c_id", nullable = false)
    @JsonProperty("client")
    private User client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "p_id", nullable = false)
    @JsonProperty("procedure")
    private Procedure procedure;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "m_id", nullable = false)
    @JsonProperty("master")
    private User master;

    @Column(name = "feedback", length = 255)
    @JsonProperty("feedback")
    private String feedback;

    @Column(name = "date", nullable = false)
    @JsonProperty("date")
    private LocalDate date;

    @Column(name = "time", nullable = false)
    @JsonProperty("time")
    private LocalTime time;

    @Column(name = "status", length = 10)
    @JsonProperty("status")
    private String status;

}
