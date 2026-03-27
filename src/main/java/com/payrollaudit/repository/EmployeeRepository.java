package com.payrollaudit.repository;

import com.payrollaudit.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartment(String department);
    List<Employee> findByStatus(Employee.EmploymentStatus status);

    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' ORDER BY e.lastName")
    List<Employee> findAllActiveEmployees();
}
