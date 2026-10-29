package org.example.adresarzam.service;

import org.example.adresarzam.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> findAll();
    Employee findById(Integer id);
    Employee save(Employee employee);
    void deleteById(Integer id);
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Employee> findByFirstNameAndLastName(String firstName, String lastName);
    List<Employee> getSortedEmployees();
}
