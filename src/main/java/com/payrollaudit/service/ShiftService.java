package com.payrollaudit.service;

import com.payrollaudit.dto.ShiftRequest;
import com.payrollaudit.exception.ResourceNotFoundException;
import com.payrollaudit.model.*;
import com.payrollaudit.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditEngineService auditEngineService;

    public ShiftService(ShiftRepository shiftRepository,
                        EmployeeRepository employeeRepository,
                        AuditEngineService auditEngineService) {
        this.shiftRepository = shiftRepository;
        this.employeeRepository = employeeRepository;
        this.auditEngineService = auditEngineService;
    }

    @Transactional
    public Shift clockIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        Shift shift = new Shift();
        shift.setEmployee(employee);
        shift.setClockIn(LocalDateTime.now());
        shift.setStatus(Shift.ShiftStatus.IN_PROGRESS);
        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift clockOut(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + shiftId));

        if (shift.getClockOut() != null) {
            throw new IllegalStateException("Shift already clocked out");
        }

        shift.setClockOut(LocalDateTime.now());
        shift.setStatus(Shift.ShiftStatus.COMPLETED);
        shiftRepository.save(shift);
        auditEngineService.analyzeShift(shift);
        return shift;
    }

    @Transactional
    public Shift createShift(ShiftRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Shift shift = new Shift();
        shift.setEmployee(employee);
        shift.setClockIn(request.getClockIn());
        shift.setClockOut(request.getClockOut());
        shift.setStatus(request.getClockOut() != null ? Shift.ShiftStatus.COMPLETED : Shift.ShiftStatus.IN_PROGRESS);
        shiftRepository.save(shift);

        if (request.getClockOut() != null) {
            auditEngineService.analyzeShift(shift);
        }
        return shift;
    }

    public List<Shift> getFlaggedShifts() {
        return shiftRepository.findByFlaggedTrue();
    }

    public List<Shift> getEmployeeShifts(Long employeeId) {
        return shiftRepository.findByEmployeeId(employeeId);
    }

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }
}
