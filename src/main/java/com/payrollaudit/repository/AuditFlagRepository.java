package com.payrollaudit.repository;

import com.payrollaudit.model.AuditFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditFlagRepository extends JpaRepository<AuditFlag, Long> {

    List<AuditFlag> findByEmployeeId(Long employeeId);

    List<AuditFlag> findByResolutionStatus(AuditFlag.ResolutionStatus status);

    List<AuditFlag> findBySeverity(AuditFlag.Severity severity);

    List<AuditFlag> findByFlagType(AuditFlag.FlagType flagType);

    @Query("SELECT COUNT(a) FROM AuditFlag a WHERE a.resolutionStatus = 'OPEN'")
    long countOpenFlags();

    @Query("SELECT a.flagType, COUNT(a) FROM AuditFlag a GROUP BY a.flagType")
    List<Object[]> countByFlagType();
}
