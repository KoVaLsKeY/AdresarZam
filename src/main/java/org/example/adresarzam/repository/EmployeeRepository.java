package org.example.adresarzam.repository;

import org.example.adresarzam.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findByFirstNameAndLastName(String firstName, String lastName);
    List<Employee> findAllByOrderByLastNameAsc();
}
