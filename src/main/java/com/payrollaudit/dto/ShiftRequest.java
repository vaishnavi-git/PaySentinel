package com.payrollaudit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShiftRequest {
    private Long employeeId;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDateTime getClockIn() { return clockIn; }
    public void setClockIn(LocalDateTime clockIn) { this.clockIn = clockIn; }
    public LocalDateTime getClockOut() { return clockOut; }
    public void setClockOut(LocalDateTime clockOut) { this.clockOut = clockOut; }
}
