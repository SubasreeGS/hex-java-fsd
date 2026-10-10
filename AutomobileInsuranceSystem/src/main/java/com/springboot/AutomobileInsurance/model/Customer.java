package com.springboot.AutomobileInsurance.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false,length = 255)
    private String name;

    @Column(name = "phone_no", nullable = false, length = 15)
    private String phoneNo;

    @Column(name = "address",nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "aadhaar_no", nullable = false, unique = true, length = 12)
    private String aadhaarNo;

    @Column(name = "pan_no", nullable = false, unique = true, length = 10)
    private String panNo;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
}
