package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

@Query("""
select c
from Customer c
join c.user u
where u.username=?1
""")
    Customer getByCustomerUsername(String customerUsername);


}