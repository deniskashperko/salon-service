package com.example.salon_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_seq")
    @SequenceGenerator(name = "users_seq", sequenceName = "users_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @JsonIgnore
    @Column(name = "login", nullable = false, length = 30, unique = true)
    private String login;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "surname", nullable = false, length = 30)
    private String surname;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role", nullable = false)
    private Role role;

    @Column(name = "phone_num", length = 10)
    private String phoneNum;

    @ManyToMany
    @JoinTable(
            name = "p_list",
            joinColumns = @JoinColumn(name = "m_id"),
            inverseJoinColumns = @JoinColumn(name = "p_id")
    )
    @JsonIgnore // для предотвращения бесконечной рекурсии при сериализации
    private List<Procedure> procedures;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserContact contact;

    @Transient
    @JsonIgnore
    public List<Procedure> getUnavailableProcedures(List<Procedure> allProcedures) {
        return allProcedures.stream()
                .filter(p -> !procedures.contains(p))
                .toList();
    }


}
