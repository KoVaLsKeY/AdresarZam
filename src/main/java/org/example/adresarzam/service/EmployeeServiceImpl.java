package org.example.adresarzam.service;

import org.example.adresarzam.entity.Employee;
import org.example.adresarzam.exception.EmailAlreadyExistsException;
import org.example.adresarzam.exception.ObjectNotFoundException;
import org.example.adresarzam.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(Integer id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Employee", id));
    }

    @Transactional
    @Override
    public Employee save(Employee employee) {
        // Kontrola duplicitného emailu pri vytváraní nového študenta
        if (employee.getId() == null) {
            if (employeeRepository.existsByEmail((employee.getEmail())) {
                throw new EmailAlreadyExistsException(employee.getEmail());
            }
        } else {
            // Pri aktualizácii skontroluje, či email nepatrí inému študentovi
            Employee existingWithEmail = employeeRepository.findByEmail(employee.getEmail()).orElse(null);
            if (existingWithEmail != null && !Objects.equals(existingWithEmail.getId(), employee.getId())) {
                throw new EmailAlreadyExistsException(employee.getEmail());
            }
        }

        return employeeRepository.save(employee);
    }

    @Transactional
    @Override
    public void deleteById(Integer id) {
        if (!employeeRepository.existsById(id)) {
            throw new ObjectNotFoundException("Študent", id);
        }
        employeeRepository.deleteById(id);
    }

    @Override
    public List<Employee> findByFirstNameAndLastName(String firstName, String lastName) {
        return employeeRepository.findByFirstNameAndLastName(firstName, lastName);
    }

    @Override
    public List<Employee> getSortedEmployees() {
        return employeeRepository.findAllByOrderByLastNameAsc();
    }
}
