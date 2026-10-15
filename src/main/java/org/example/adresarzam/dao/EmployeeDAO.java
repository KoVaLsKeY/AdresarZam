package org.example.adresarzam.dao;

import org.example.adresarzam.entity.Employee;
import java.util.List;

public interface EmployeeDAO {
    List<Employee> findAll();
}