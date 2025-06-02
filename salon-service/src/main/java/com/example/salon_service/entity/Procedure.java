package com.example.salon_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "procedures")
@Data
public class Procedure implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JsonProperty("id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    @JsonProperty("name")
    private String name;

    @Column(name = "price", nullable = false)
    @JsonProperty("price")
    private Integer price;

    @Column(name = "ex_time", nullable = false)
    @JsonProperty("ex_time")
    private LocalTime exTime;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "procedures")
    private List<User> masters;


}
