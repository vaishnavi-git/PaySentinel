package com.payrollaudit.config;

import com.payrollaudit.model.*;
import com.payrollaudit.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedData(UserRepository userRepo,
                               EmployeeRepository empRepo,
                               ShiftRepository shiftRepo,
                               PasswordEncoder encoder) {
        return args -> {
            // Seed admin user
            if (!userRepo.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setEmail("admin@payrollaudit.com");
                admin.setRoles(Set.of("ADMIN", "PAYROLL_MANAGER"));
                userRepo.save(admin);
            }

            // Seed payroll manager
            if (!userRepo.existsByUsername("manager")) {
                User mgr = new User();
                mgr.setUsername("manager");
                mgr.setPassword(encoder.encode("manager123"));
                mgr.setEmail("manager@payrollaudit.com");
                mgr.setRoles(Set.of("PAYROLL_MANAGER"));
                userRepo.save(mgr);
            }

            // Seed employees
            Employee e1 = new Employee();
            e1.setFirstName("Sarah"); e1.setLastName("Connor");
            e1.setEmail("s.connor@company.com");
            e1.setDepartment("Engineering"); e1.setRole("Software Engineer");
            e1.setHourlyRate(new BigDecimal("55.00"));
            e1.setHireDate(LocalDate.of(2021, 3, 15));
            empRepo.save(e1);

            Employee e2 = new Employee();
            e2.setFirstName("John"); e2.setLastName("Reese");
            e2.setEmail("j.reese@company.com");
            e2.setDepartment("Operations"); e2.setRole("Operations Analyst");
            e2.setHourlyRate(new BigDecimal("42.00"));
            e2.setHireDate(LocalDate.of(2020, 7, 1));
            empRepo.save(e2);

            Employee e3 = new Employee();
            e3.setFirstName("Amy"); e3.setLastName("Pond");
            e3.setEmail("a.pond@company.com");
            e3.setDepartment("HR"); e3.setRole("HR Specialist");
            e3.setHourlyRate(new BigDecimal("38.50"));
            e3.setHireDate(LocalDate.of(2022, 1, 10));
            empRepo.save(e3);

            // Seed a normal shift
            Shift s1 = new Shift();
            s1.setEmployee(e1);
            s1.setClockIn(LocalDateTime.now().minusHours(8));
            s1.setClockOut(LocalDateTime.now());
            s1.setHoursWorked(new BigDecimal("8.00"));
            s1.setGrossPay(new BigDecimal("440.00"));
            s1.setStatus(Shift.ShiftStatus.COMPLETED);
            shiftRepo.save(s1);

            // Seed a shift that will trigger overtime flag
            Shift s2 = new Shift();
            s2.setEmployee(e2);
            s2.setClockIn(LocalDateTime.now().minusHours(14));
            s2.setClockOut(LocalDateTime.now());
            s2.setHoursWorked(new BigDecimal("14.00"));
            s2.setGrossPay(new BigDecimal("714.00"));
            s2.setStatus(Shift.ShiftStatus.ANOMALY_DETECTED);
            s2.setFlagged(true);
            s2.setFlagReason("Shift duration of 14.00 hours exceeds maximum allowed 12.0 hours");
            shiftRepo.save(s2);

            // Seed a missing punch shift
            Shift s3 = new Shift();
            s3.setEmployee(e3);
            s3.setClockIn(LocalDateTime.now().minusHours(16));
            s3.setStatus(Shift.ShiftStatus.MISSING_PUNCH);
            s3.setFlagged(true);
            s3.setFlagReason("Clock-out not recorded after 14 hours");
            shiftRepo.save(s3);

            System.out.println(">>> Payroll Audit API seeded. Login: admin/admin123 or manager/manager123");
        };
    }
}
