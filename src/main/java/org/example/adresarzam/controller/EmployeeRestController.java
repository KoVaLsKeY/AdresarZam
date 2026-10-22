package org.example.adresarzam.controller;

import org.example.adresarzam.entity.Employee;
import org.example.adresarzam.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeRestController {

    private EmployeeService employeeService;

    @Autowired
    public EmployeeRestController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public List<Employee> findAll() {
        return employeeService.findAll();
    }
    @PutMapping("/employees/{id}")
    public Employee updateEmployee(
            @PathVariable("id") Integer id,
            @RequestBody Employee employee) {

        Employee existingEmployee = employeeService.findById(id);

        if (existingEmployee == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Employee not found: " + id
            );
        }

        employee.setId(id);

        return employeeService.save(employee);
    }
}