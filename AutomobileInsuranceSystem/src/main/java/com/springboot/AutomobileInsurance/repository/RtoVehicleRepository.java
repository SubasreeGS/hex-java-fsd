package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.RtoVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RtoVehicleRepository extends JpaRepository<RtoVehicle, Long> {

    Optional<RtoVehicle> findByRegistrationNo(String registrationNo);
}