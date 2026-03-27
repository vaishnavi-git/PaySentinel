package com.payrollaudit.repository;

import com.payrollaudit.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    List<Shift> findByEmployeeId(Long employeeId);

    List<Shift> findByFlaggedTrue();

    List<Shift> findByStatus(Shift.ShiftStatus status);

    @Query("SELECT s FROM Shift s WHERE s.employee.id = :empId AND s.clockIn BETWEEN :start AND :end")
    List<Shift> findByEmployeeAndDateRange(
        @Param("empId") Long employeeId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT s FROM Shift s WHERE s.hoursWorked > :hours")
    List<Shift> findShiftsExceedingHours(@Param("hours") double hours);

    @Query("SELECT s FROM Shift s WHERE s.clockOut IS NULL AND s.clockIn < :threshold")
    List<Shift> findMissingPunchShifts(@Param("threshold") LocalDateTime threshold);
}
