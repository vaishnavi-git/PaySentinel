package com.payrollaudit.controller;

import com.payrollaudit.dto.ShiftRequest;
import com.payrollaudit.model.Shift;
import com.payrollaudit.service.ShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<List<Shift>> getAllShifts() {
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/flagged")
    public ResponseEntity<List<Shift>> getFlaggedShifts() {
        return ResponseEntity.ok(shiftService.getFlaggedShifts());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Shift>> getEmployeeShifts(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftService.getEmployeeShifts(employeeId));
    }

    @PostMapping("/clock-in/{employeeId}")
    public ResponseEntity<Shift> clockIn(@PathVariable Long employeeId) {
        return ResponseEntity.ok(shiftService.clockIn(employeeId));
    }

    @PutMapping("/clock-out/{shiftId}")
    public ResponseEntity<Shift> clockOut(@PathVariable Long shiftId) {
        return ResponseEntity.ok(shiftService.clockOut(shiftId));
    }

    @PostMapping
    public ResponseEntity<Shift> createShift(@RequestBody ShiftRequest request) {
        return ResponseEntity.ok(shiftService.createShift(request));
    }
}
