package com.springboot.AutomobileInsurance.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "inspector")
public class Inspector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id",nullable = false, unique = true)
    private User user;

    @Column(name = "name",nullable = false, length = 255)
    private String name;
}