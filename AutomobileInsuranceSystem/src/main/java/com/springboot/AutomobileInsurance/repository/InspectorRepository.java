package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.InspectorDropdownDto;
import com.springboot.AutomobileInsurance.model.Inspector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectorRepository extends JpaRepository<Inspector, Long> {

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.InspectorDropdownDto(
            i.id,
            i.name,
            i.contact
        )
        FROM Inspector i
        ORDER BY i.name ASC
    """)
    List<InspectorDropdownDto> findAllForDropdown();

    @Query("SELECT i FROM Inspector i WHERE i.user.username = ?1")
    Optional<Inspector> findByUserUsername(String username);

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.InspectorDropdownDto(
            i.id,
            i.name,
            i.contact
        )
        FROM Inspector i
        JOIN i.officer o
        JOIN o.user u
        WHERE u.username = ?1
        ORDER BY i.name ASC
    """)
    List<InspectorDropdownDto> findMyInspectorsForDropdown(String officerUsername);
}