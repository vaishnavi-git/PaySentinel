package com.payrollaudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PayrollAuditApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayrollAuditApplication.class, args);
    }
}
