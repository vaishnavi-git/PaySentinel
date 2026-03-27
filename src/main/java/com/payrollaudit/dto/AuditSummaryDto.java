package com.payrollaudit.dto;

import java.util.Map;

public class AuditSummaryDto {
    private long totalShifts;
    private long flaggedShifts;
    private long openFlags;
    private long criticalFlags;
    private Map<String, Long> flagsByType;
    private double flagRate;

    public AuditSummaryDto(long totalShifts, long flaggedShifts, long openFlags,
                           long criticalFlags, Map<String, Long> flagsByType) {
        this.totalShifts = totalShifts;
        this.flaggedShifts = flaggedShifts;
        this.openFlags = openFlags;
        this.criticalFlags = criticalFlags;
        this.flagsByType = flagsByType;
        this.flagRate = totalShifts > 0 ? (double) flaggedShifts / totalShifts * 100 : 0;
    }

    public long getTotalShifts() { return totalShifts; }
    public long getFlaggedShifts() { return flaggedShifts; }
    public long getOpenFlags() { return openFlags; }
    public long getCriticalFlags() { return criticalFlags; }
    public Map<String, Long> getFlagsByType() { return flagsByType; }
    public double getFlagRate() { return flagRate; }
}
