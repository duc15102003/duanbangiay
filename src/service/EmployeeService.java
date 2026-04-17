package service;

import dao.EmployeeDAO;
import entity.Employee;
import entity.filter.EmployeeFilter;
import enums.RoleEnum;
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
        
        request.setCode(generateCode());
        return employeeDAO.insert(request);
    }

    public boolean update(Employee request) {
        if (!EmployeeValidator.validateUpdate(request)) {
            return false;
        }
        return employeeDAO.update(request);
    }

    public boolean delete(int id, int currentUserId) {
        if (id == currentUserId) {
            throw new RuntimeException("Không thể xoá chính mình");
        }

        Employee target = employeeDAO.findById(id);

        if (target == null) {
            throw new RuntimeException("Nhân viên không tồn tại");
        }

        if (target.getRole() == RoleEnum.ADMIN 
                || target.getRole() == RoleEnum.MANAGER) {
            throw new RuntimeException("Không thể xoá ADMIN hoặc MANAGER");
        }

        return employeeDAO.delete(id);
    }
    
    public String generateCode() {

        String maxCode = employeeDAO.getMaxCode();

        if (maxCode == null) {
            return "EM001";
        }

        int number = Integer.parseInt(maxCode.replace("EM", ""));
        number++;

        return "EM" + String.format("%03d", number);
    }
}
