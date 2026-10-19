package com.springboot.AutomobileInsurance.model;

import com.springboot.AutomobileInsurance.enums.InspectionResult;
import com.springboot.AutomobileInsurance.enums.InspectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity

public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InspectionResult result;

    @Column(name = "recommended_idv", precision = 10, scale = 2)
    private BigDecimal recommendedIdv;

    @Column(name = "approved_idv", precision = 10, scale = 2)
    private BigDecimal approvedIdv;

    @Column(name = "inspection_photos_url", length = 500)
    private String inspectionPhotosUrl;

    @Column(name = "inspection_notes")
    private String inspectionNotes;

    @Enumerated(EnumType.STRING)
    private InspectionStatus status;

    @OneToOne
    private Proposal proposal;

    @ManyToOne
    @JoinColumn(name = "inspector_id", nullable = false)
    private Inspector inspector;
}