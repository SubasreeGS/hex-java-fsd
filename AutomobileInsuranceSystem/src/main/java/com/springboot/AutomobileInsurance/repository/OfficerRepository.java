package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.ScopedValue;
import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {

@Query("""
select o
from Officer o
join o.user u
where u.username=?1
""")
    Officer findByOfficerUsername(String officerUsername);



}