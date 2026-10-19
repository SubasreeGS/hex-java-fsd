package com.springboot.AutomobileInsurance.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity

public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 255)
    private String name;

    @Column(name = "phone_no", nullable = false, length = 15)
    private String phoneNo;

    @Column(nullable = false,length = 500)
    private String address;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(name = "aadhaar_no", nullable = false, unique = true, length = 12)
    private String aadhaarNo;

    @Column(name = "pan_no", nullable = false, unique = true, length = 10)
    private String panNo;

    @UpdateTimestamp
    private Instant updatedAt;

    @OneToOne
    private User user;
}
