package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.FuelType;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "rto_vehicle")
public class RtoVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_no", nullable = false, unique = true, length = 20)
    private String registrationNo;

    @Column(name = "make",nullable = false, length = 50)
    private String make;

    @Column(name = "model",nullable = false, length = 50)
    private String model;

    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "engine_capacity", nullable = false)
    private Integer engineCapacity;

    @Column(name = "vin_no", nullable = false, unique = true, length = 17)
    private String vinNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

}