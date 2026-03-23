package service;

import dao.EmployeeDAO;
import entity.Employee;
import entity.filter.EmployeeFilter;
import java.util.List;
import validator.EmployeeValidator;

public class EmployeeService {
    
    private EmployeeDAO employeeDAO = new EmployeeDAO();

    public List<Employee> findAll(EmployeeFilter filter) {
        return employeeDAO.findAll(filter);
    }

    public Employee findById(int id) {
        return employeeDAO.findById(id);
    }

    public boolean insert(Employee request) {
        if (!EmployeeValidator.validateCreate(request)) {
            return false;
        }
        return employeeDAO.insert(request);
    }

    public boolean update(Employee request) {
        if (!EmployeeValidator.validateUpdate(request)) {
            return false;
        }
        return employeeDAO.update(request);
    }

    public boolean delete(int id) {
        return employeeDAO.delete(id);
    }
}
