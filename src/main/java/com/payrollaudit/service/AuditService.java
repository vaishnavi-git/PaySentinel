package com.payrollaudit.service;

import com.payrollaudit.dto.AuditSummaryDto;
import com.payrollaudit.model.AuditFlag;
import com.payrollaudit.repository.AuditFlagRepository;
import com.payrollaudit.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {

    private final AuditFlagRepository auditFlagRepository;
    private final ShiftRepository shiftRepository;

    public AuditService(AuditFlagRepository auditFlagRepository, ShiftRepository shiftRepository) {
        this.auditFlagRepository = auditFlagRepository;
        this.shiftRepository = shiftRepository;
    }

    public AuditSummaryDto getSummary() {
        long totalShifts = shiftRepository.count();
        long flaggedShifts = shiftRepository.findByFlaggedTrue().size();
        long openFlags = auditFlagRepository.countOpenFlags();
        long criticalFlags = auditFlagRepository.findBySeverity(AuditFlag.Severity.CRITICAL).size();

        List<Object[]> flagCounts = auditFlagRepository.countByFlagType();
        Map<String, Long> flagsByType = new HashMap<>();
        for (Object[] row : flagCounts) {
            flagsByType.put(row[0].toString(), (Long) row[1]);
        }

        return new AuditSummaryDto(totalShifts, flaggedShifts, openFlags, criticalFlags, flagsByType);
    }

    public List<AuditFlag> getOpenFlags() {
        return auditFlagRepository.findByResolutionStatus(AuditFlag.ResolutionStatus.OPEN);
    }

    public List<AuditFlag> getFlagsByEmployee(Long employeeId) {
        return auditFlagRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public AuditFlag resolveFlag(Long flagId, String resolvedBy) {
        AuditFlag flag = auditFlagRepository.findById(flagId)
                .orElseThrow(() -> new RuntimeException("Flag not found: " + flagId));
        flag.setResolutionStatus(AuditFlag.ResolutionStatus.RESOLVED);
        flag.setResolvedBy(resolvedBy);
        flag.setResolvedAt(LocalDateTime.now());
        return auditFlagRepository.save(flag);
    }

    @Transactional
    public AuditFlag dismissFlag(Long flagId, String dismissedBy) {
        AuditFlag flag = auditFlagRepository.findById(flagId)
                .orElseThrow(() -> new RuntimeException("Flag not found: " + flagId));
        flag.setResolutionStatus(AuditFlag.ResolutionStatus.DISMISSED);
        flag.setResolvedBy(dismissedBy);
        flag.setResolvedAt(LocalDateTime.now());
        return auditFlagRepository.save(flag);
    }
}
