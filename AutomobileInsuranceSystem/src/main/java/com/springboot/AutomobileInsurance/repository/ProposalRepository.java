package com.springboot.AutomobileInsurance.repository;

import com.springboot.AutomobileInsurance.dto.response.*;
import com.springboot.AutomobileInsurance.enums.PolicyType;
import com.springboot.AutomobileInsurance.enums.ProposalStatus;
import com.springboot.AutomobileInsurance.model.Proposal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByOfficerId(Long officerId);

    @Query("""
        SELECT new com.springboot.AutomobileInsurance.dto.response.ProposalInfoDto(
            p.id,
            p.status,
            p.policyType,
            p.isZeroDep,
            p.isEngineProtect,
            p.isRoadsideAssist,
            ins.name,
            ins.email,
            ins.contact,
            v.registrationNo,
            v.make,
            v.model,
            p.rejectionRemarks
        )
        FROM Proposal p
        JOIN p.vehicle v
        LEFT JOIN Inspection i ON i.proposal = p
        LEFT JOIN i.inspector ins
        WHERE p.customer.id = ?1
    """)
    List<ProposalInfoDto> getProposalByCustomerId(Long customerId, Pageable pageable);

@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.ProposalInfoDto(
            p.id,
            p.status,
            p.policyType,
            p.isZeroDep,
            p.isEngineProtect,
            p.isRoadsideAssist,
            ins.name,
            ins.email,
            ins.contact,
            v.registrationNo,
            v.make,
            v.model,
            p.rejectionRemarks
        )
        FROM Proposal p
        JOIN p.customer c
        JOIN c.user u
        JOIN p.vehicle v
        LEFT JOIN Inspection i ON i.proposal = p
        LEFT JOIN i.inspector ins
        WHERE u.username = ?1
""")
    List<ProposalInfoDto> getByCustomerUsername(String customerUsername, Pageable pageable);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV2(
            p.id,
            c.name,
            c.phoneNo,
            c.address,
            v.registrationNo,
            v.make,
            v.model,
            v.manufactureYear,
            v.engineCapacity,
            v.vinNo,
            v.fuelType,
            p.rcDocUrl,
            p.policyType,
            p.isZeroDep,
            p.isEngineProtect,
            p.isRoadsideAssist
        )
        FROM Inspection i
        JOIN i.proposal p
        JOIN p.customer c
        JOIN p.vehicle v
        JOIN i.inspector ins
        JOIN ins.user u
        WHERE u.username = ?1
""")
Page<ProposalInfoDtoV2> getByInspectorUsername(String inspectorUsername, Pageable pageable);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV3(
            p.id,
            p.status,
            p.policyType,
            p.isZeroDep,
            p.isEngineProtect,
            p.isRoadsideAssist,
            c.name,
            c.phoneNo,
            c.address,
            c.dob,
            c.aadhaarNo,
            c.panNo,
            v.registrationNo,
            v.make,
            v.model,
            v.manufactureYear,
            v.fuelType,
            p.rcDocUrl,
            v.vinNo,
            v.engineCapacity,
            ins.name
        )
        FROM Proposal p
        JOIN p.officer o
        JOIN o.user u
        JOIN p.customer c
        JOIN p.vehicle v
        LEFT JOIN Inspection i ON i.proposal = p
        LEFT JOIN i.inspector ins
        WHERE u.username =?1
""")
    List<ProposalInfoDtoV3> getByOfficerUsername(String officerUsername, Pageable pageable);


@Query("""
SELECT new com.springboot.AutomobileInsurance.dto.response.ProposalInfoDtoV3(
        p.id,
        p.status,
        p.policyType,
        p.isZeroDep,
        p.isEngineProtect,
        p.isRoadsideAssist,
        c.name,
        c.phoneNo,
        c.address,
        c.dob,
        c.aadhaarNo,
        c.panNo,
        v.registrationNo,
        v.make,
        v.model,
        v.manufactureYear,
        v.fuelType,
        p.rcDocUrl,
        v.vinNo,
        v.engineCapacity,
        ins.name
    )
    FROM Proposal p
    JOIN p.customer c
    JOIN p.vehicle v
    LEFT JOIN p.officer o
    LEFT JOIN o.user u
    LEFT JOIN Inspection i ON i.proposal = p
    LEFT JOIN i.inspector ins
    WHERE p.status = ?1
    AND (p.officer IS NULL OR u.username = ?2)
""")
Page<ProposalInfoDtoV3> getByProposalStatus(ProposalStatus status, String officerUsername, Pageable pageable);


    @Query("SELECT new com.springboot.AutomobileInsurance.dto.response.StatDto(CAST(p.status AS string), COUNT(p)) FROM Proposal p GROUP BY p.status")
    List<StatDto> getProposalsCountByStatus();

    @Query("""
        select
            p.id as id,
            c.name as customerName,
            veh.registrationNo as vehicleNumber,
            p.policyType as policyType,
            p.status as status,
            p.createdAt as createdAt
        from Proposal p
        left join p.customer c
        left join p.vehicle veh
        where (?1 IS NULL OR p.policyType = ?1)
          and (?2 IS NULL OR p.status = ?2)
          and (?3 IS NULL OR p.createdAt >= ?3)
          and (?4 IS NULL OR p.createdAt <= ?4)
        order by p.id desc
    """)
    Page<ProposalRespDto> dynamicFilterProposals(
            PolicyType policyType,
            ProposalStatus status,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );


}