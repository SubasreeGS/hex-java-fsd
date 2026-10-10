package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(Long userId);

    boolean existsByAadhaarNo(String aadhaarNo);

    boolean existsByPanNo(String panNo);
}