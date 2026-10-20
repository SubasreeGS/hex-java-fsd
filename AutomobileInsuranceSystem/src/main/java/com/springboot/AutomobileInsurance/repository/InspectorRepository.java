package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectorRepository extends JpaRepository<Inspector, Long> {

}