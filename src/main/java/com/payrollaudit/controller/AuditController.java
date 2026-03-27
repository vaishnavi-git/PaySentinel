package com.payrollaudit.controller;

import com.payrollaudit.dto.AuditSummaryDto;
import com.payrollaudit.model.AuditFlag;
import com.payrollaudit.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/summary")
    public ResponseEntity<AuditSummaryDto> getSummary() {
        return ResponseEntity.ok(auditService.getSummary());
    }

    @GetMapping("/flags")
    public ResponseEntity<List<AuditFlag>> getOpenFlags() {
        return ResponseEntity.ok(auditService.getOpenFlags());
    }

    @GetMapping("/flags/employee/{employeeId}")
    public ResponseEntity<List<AuditFlag>> getFlagsByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(auditService.getFlagsByEmployee(employeeId));
    }

    @PutMapping("/flags/{flagId}/resolve")
    public ResponseEntity<AuditFlag> resolveFlag(@PathVariable Long flagId,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(auditService.resolveFlag(flagId, user.getUsername()));
    }

    @PutMapping("/flags/{flagId}/dismiss")
    public ResponseEntity<AuditFlag> dismissFlag(@PathVariable Long flagId,
                                                  @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(auditService.dismissFlag(flagId, user.getUsername()));
    }
}
