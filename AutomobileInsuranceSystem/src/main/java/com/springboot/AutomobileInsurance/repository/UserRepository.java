package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("""
            select u
            from User u
            where u.username=?1
            """)
    User loadUserByUsername(String username);

    boolean existsByUsername(String username);
}