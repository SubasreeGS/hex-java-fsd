package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {

}