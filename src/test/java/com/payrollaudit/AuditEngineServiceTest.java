package com.payrollaudit;

import com.payrollaudit.model.*;
import com.payrollaudit.repository.*;
import com.payrollaudit.service.AuditEngineService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEngineServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private AuditFlagRepository auditFlagRepository;

    @InjectMocks
    private AuditEngineService auditEngineService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setFirstName("Jane");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("jane.doe@company.com");
        testEmployee.setDepartment("Engineering");
        testEmployee.setRole("Engineer");
        testEmployee.setHourlyRate(new BigDecimal("50.00"));
        testEmployee.setHireDate(LocalDate.of(2021, 1, 1));
    }

    @Test
    @DisplayName("Normal 8-hour shift should not generate any audit flags")
    void testNormalShift_NoFlags() {
        Shift shift = buildShift(8);
        when(shiftRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(List.of(shift));

        auditEngineService.analyzeShift(shift);

        verify(auditFlagRepository, never()).save(any());
        assertFalse(shift.isFlagged());
        assertEquals(new BigDecimal("400.00"), shift.getGrossPay());
    }

    @Test
    @DisplayName("Shift exceeding 12 hours should flag EXCESSIVE_HOURS")
    void testExcessiveHoursShift_ShouldFlag() {
        Shift shift = buildShift(13);
        when(shiftRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        auditEngineService.analyzeShift(shift);

        assertTrue(shift.isFlagged());
        verify(auditFlagRepository, atLeastOnce()).save(argThat(flag ->
                flag.getFlagType() == AuditFlag.FlagType.EXCESSIVE_HOURS
        ));
    }

    @Test
    @DisplayName("Shift with overtime between 8-12 hours should flag OVERTIME_VIOLATION")
    void testOvertimeShift_ShouldFlagOvertimeViolation() {
        Shift shift = buildShift(10);
        when(shiftRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        auditEngineService.analyzeShift(shift);

        assertTrue(shift.isFlagged());
        verify(auditFlagRepository, atLeastOnce()).save(argThat(flag ->
                flag.getFlagType() == AuditFlag.FlagType.OVERTIME_VIOLATION
        ));
    }

    @Test
    @DisplayName("Overtime pay calculation should apply 1.5x rate for hours over 8")
    void testOvertimePayCalculation() {
        Shift shift = buildShift(10);
        when(shiftRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        auditEngineService.analyzeShift(shift);

        // 8h * $50 + 2h * $75 = $400 + $150 = $550
        assertEquals(new BigDecimal("550.00"), shift.getGrossPay());
    }

    @Test
    @DisplayName("Weekly hours over 40 should flag UNAUTHORIZED_OVERTIME as CRITICAL")
    void testWeeklyOvertimeLimit_ShouldFlagCritical() {
        Shift currentShift = buildShift(9);

        Shift prevShift1 = buildShift(9);
        Shift prevShift2 = buildShift(9);
        Shift prevShift3 = buildShift(9);
        Shift prevShift4 = buildShift(9);

        when(shiftRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(List.of(prevShift1, prevShift2, prevShift3, prevShift4, currentShift));

        auditEngineService.analyzeShift(currentShift);

        verify(auditFlagRepository, atLeastOnce()).save(argThat(flag ->
                flag.getFlagType() == AuditFlag.FlagType.UNAUTHORIZED_OVERTIME &&
                flag.getSeverity() == AuditFlag.Severity.CRITICAL
        ));
    }

    @Test
    @DisplayName("Shift hours worked should be calculated correctly")
    void testHoursWorkedCalculation() {
        Shift shift = buildShift(8);
        when(shiftRepository.findByEmployeeAndDateRange(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        auditEngineService.analyzeShift(shift);

        assertNotNull(shift.getHoursWorked());
        assertEquals(0, shift.getHoursWorked().compareTo(new BigDecimal("8.00")));
    }

    private Shift buildShift(int hours) {
        Shift shift = new Shift();
        shift.setEmployee(testEmployee);
        shift.setClockIn(LocalDateTime.now().minusHours(hours));
        shift.setClockOut(LocalDateTime.now());
        shift.setStatus(Shift.ShiftStatus.COMPLETED);
        return shift;
    }
}
