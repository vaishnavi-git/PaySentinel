package com.payrollaudit.service;

import com.payrollaudit.model.*;
import com.payrollaudit.repository.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditEngineService {

    private static final double MAX_SHIFT_HOURS = 12.0;
    private static final double OVERTIME_THRESHOLD = 8.0;
    private static final double WEEKLY_OVERTIME_THRESHOLD = 40.0;
    private static final long MISSING_PUNCH_THRESHOLD_HOURS = 14;

    private final ShiftRepository shiftRepository;
    private final AuditFlagRepository auditFlagRepository;

    public AuditEngineService(ShiftRepository shiftRepository,
                              AuditFlagRepository auditFlagRepository) {
        this.shiftRepository = shiftRepository;
        this.auditFlagRepository = auditFlagRepository;
    }

    @Transactional
    public void analyzeShift(Shift shift) {
        if (shift.getClockOut() == null) return;

        double hours = Duration.between(shift.getClockIn(), shift.getClockOut())
                .toMinutes() / 60.0;

        shift.setHoursWorked(BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP));

        BigDecimal gross = shift.getEmployee().getHourlyRate()
                .multiply(BigDecimal.valueOf(Math.min(hours, OVERTIME_THRESHOLD)));

        if (hours > OVERTIME_THRESHOLD) {
            BigDecimal overtimePay = shift.getEmployee().getHourlyRate()
                    .multiply(BigDecimal.valueOf(1.5))
                    .multiply(BigDecimal.valueOf(hours - OVERTIME_THRESHOLD));
            gross = gross.add(overtimePay);
        }

        shift.setGrossPay(gross.setScale(2, RoundingMode.HALF_UP));

        if (hours > MAX_SHIFT_HOURS) {
            flagShift(shift, AuditFlag.FlagType.EXCESSIVE_HOURS, AuditFlag.Severity.HIGH,
                    String.format("Shift duration of %.2f hours exceeds maximum allowed %.1f hours", hours, MAX_SHIFT_HOURS));
        }

        if (hours > OVERTIME_THRESHOLD) {
            flagShift(shift, AuditFlag.FlagType.OVERTIME_VIOLATION, AuditFlag.Severity.MEDIUM,
                    String.format("Shift includes %.2f hours of overtime (threshold: %.1f hours)", hours - OVERTIME_THRESHOLD, OVERTIME_THRESHOLD));
        }

        checkWeeklyOvertime(shift);
        shiftRepository.save(shift);
    }

    private void checkWeeklyOvertime(Shift shift) {
        LocalDateTime weekStart = shift.getClockIn().minusDays(shift.getClockIn().getDayOfWeek().getValue() - 1);
        List<Shift> weekShifts = shiftRepository.findByEmployeeAndDateRange(
                shift.getEmployee().getId(), weekStart, shift.getClockOut());

        double weeklyTotal = weekShifts.stream()
                .filter(s -> s.getHoursWorked() != null)
                .mapToDouble(s -> s.getHoursWorked().doubleValue())
                .sum();

        if (weeklyTotal > WEEKLY_OVERTIME_THRESHOLD) {
            flagShift(shift, AuditFlag.FlagType.UNAUTHORIZED_OVERTIME, AuditFlag.Severity.CRITICAL,
                    String.format("Employee has accumulated %.2f hours this week, exceeding the %.1f hour weekly limit",
                            weeklyTotal, WEEKLY_OVERTIME_THRESHOLD));
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void detectMissingPunches() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(MISSING_PUNCH_THRESHOLD_HOURS);
        List<Shift> missingPunches = shiftRepository.findMissingPunchShifts(threshold);

        for (Shift shift : missingPunches) {
            if (shift.getStatus() != Shift.ShiftStatus.MISSING_PUNCH) {
                shift.setStatus(Shift.ShiftStatus.MISSING_PUNCH);
                shift.setFlagged(true);
                shift.setFlagReason("Clock-out not recorded after " + MISSING_PUNCH_THRESHOLD_HOURS + " hours");
                flagShift(shift, AuditFlag.FlagType.MISSING_PUNCH, AuditFlag.Severity.HIGH,
                        "Employee clocked in at " + shift.getClockIn() + " with no corresponding clock-out after " + MISSING_PUNCH_THRESHOLD_HOURS + " hours");
                shiftRepository.save(shift);
            }
        }
    }

    private void flagShift(Shift shift, AuditFlag.FlagType type, AuditFlag.Severity severity, String description) {
        shift.setFlagged(true);
        shift.setStatus(Shift.ShiftStatus.ANOMALY_DETECTED);

        AuditFlag flag = new AuditFlag();
        flag.setShift(shift);
        flag.setEmployee(shift.getEmployee());
        flag.setFlagType(type);
        flag.setSeverity(severity);
        flag.setDescription(description);
        flag.setDetectedAt(LocalDateTime.now());
        auditFlagRepository.save(flag);
    }
}
