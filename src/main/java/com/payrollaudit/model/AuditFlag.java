package com.payrollaudit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_flags")
public class AuditFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagType flagType;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private LocalDateTime detectedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ResolutionStatus resolutionStatus = ResolutionStatus.OPEN;

    private String resolvedBy;
    private LocalDateTime resolvedAt;

    public enum FlagType {
        OVERTIME_VIOLATION,
        MISSING_PUNCH,
        RATE_MISMATCH,
        DUPLICATE_SHIFT,
        EXCESSIVE_HOURS,
        UNAUTHORIZED_OVERTIME
    }

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    public enum ResolutionStatus { OPEN, IN_REVIEW, RESOLVED, DISMISSED }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Shift getShift() { return shift; }
    public void setShift(Shift shift) { this.shift = shift; }
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    public FlagType getFlagType() { return flagType; }
    public void setFlagType(FlagType flagType) { this.flagType = flagType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
    public ResolutionStatus getResolutionStatus() { return resolutionStatus; }
    public void setResolutionStatus(ResolutionStatus resolutionStatus) { this.resolutionStatus = resolutionStatus; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
