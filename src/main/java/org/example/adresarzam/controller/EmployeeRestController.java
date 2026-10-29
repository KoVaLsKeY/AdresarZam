package org.example.adresarzam.controller;

import jakarta.validation.Valid;
import org.example.adresarzam.entity.Employee;
import org.example.adresarzam.response.ResponseFactory;
import org.example.adresarzam.response.ResponseWrapper;
import org.example.adresarzam.service.EmployeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EmployeeRestController {
    private final EmployeeService employeeService;
    private final ResponseFactory response;

    public EmployeeRestController(EmployeeService employeeService, ResponseFactory response) {
        this.employeeService = employeeService;
        this.response = response;
    }

    @GetMapping("/employees")
    public ResponseEntity<ResponseWrapper<List<Employee>>> findAll() {
        return response.ok("Zoznam zamestnancov", employeeService.findAll());
    }

    @GetMapping("/employees/{id}")
    public ResponseEntity<ResponseWrapper<Employee>> findById(@PathVariable("id") Integer id) {
        return response.ok("Zamestnanec najdeny", employeeService.findById(id));
    }

    @PostMapping("/employees")
    public ResponseEntity<ResponseWrapper<Employee>> createEmployee(@Valid @RequestBody Employee employee) {
        employee.setId(null);
        return response.created("Zamestnanec uspesne vytvoreny", employeeService.save(employee));
    }

    @PutMapping("/employees/{id}")
    public ResponseEntity<ResponseWrapper<Employee>> updateEmployee(
            @PathVariable("id") Integer id, @Valid @RequestBody Employee employee) {
        employee.setId(id);
        return response.ok("Zamestnanec uspesne aktualizovany", employeeService.save(employee));
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ResponseWrapper<Void>> deleteEmployee(@PathVariable("id") Integer id) {
        employeeService.deleteById(id);
        // A 204 response cannot contain the JSON body required by this assignment.
        return response.ok("Zamestnanec uspesne vymazany");
    }
}
