package org.example.adresarzam.controller;

import jakarta.validation.Valid;
import org.example.adresarzam.entity.Employee;
import org.example.adresarzam.response.ResponseFactory;
import org.example.adresarzam.response.ResponseWrapper;
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
    private final ResponseFactory response;

    @Autowired
    public EmployeeRestController(EmployeeService employeeService) {
        this.employeeService = employeeService;
        this.response = response;
    }

    @GetMapping("/employees")
    public List<Employee> findAll() {
        return employeeService.findAll();
    }
    @PutMapping("/employees/{id}")
    public ResponseEntity<ResponseWrapper<Employee>> updateEmployee(
            @PathVariable("id") Integer id,
            @Valid @RequestBody Employee employee) {

        Employee existingEmployee = employeeService.findById(id);

        employeeService.findById(id);
        employee.setId(id);
        Employee updatedEmployee = employeeService.save(employee);
        return response.ok("Employee úspešne aktualizovaný", updatedEmployee);

    }
    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable("id") Integer id) {

        Employee existingEmployee = employeeService.findById(id);

        if (existingEmployee == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Employee not found: " + id
            );
        }

        employeeService.deleteById(id);
    }
    @PostMapping("/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@RequestBody Employee employee) {
        employee.setId(null);
        return employeeService.save(employee);
    }
}