package validator;

import dao.EmployeeDAO;
import entity.Employee;
import service.EmployeeService;
import javax.swing.JOptionPane;

public class EmployeeValidator {

    private static EmployeeDAO employeeDAO = new EmployeeDAO();

    public static boolean validateCreate(Employee e) {
        if (e == null) {
            JOptionPane.showMessageDialog(null, "Dữ liệu không hợp lệ");
            return false;
        }

        if (e.getCode() == null || e.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã nhân viên không được để trống");
            return false;
        }
        if (employeeDAO.existsByCode(e.getCode())) {
            JOptionPane.showMessageDialog(null, "Mã nhân viên đã tồn tại");
            return false;
        }

        if (e.getName() == null || e.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên nhân viên không được để trống");
            return false;
        }

        if (e.getUsername() == null || e.getUsername().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên đăng nhập không được để trống");
            return false;
        }
        if (employeeDAO.existsByUsername(e.getUsername())) {
            JOptionPane.showMessageDialog(null, "Tên đăng nhập đã tồn tại");
            return false;
        }

        if (e.getPassword() == null || e.getPassword().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mật khẩu không được để trống");
            return false;
        }

        if (e.getRole() == null) {
            JOptionPane.showMessageDialog(null, "Vai trò không được để trống");
            return false;
        }

        if (e.getPhone() != null && !e.getPhone().isBlank()) {
            if (!e.getPhone().matches("\\d+")) {
                JOptionPane.showMessageDialog(null, "Số điện thoại không hợp lệ");
                return false;
            }
            if (employeeDAO.existsByPhone(e.getPhone())) {
                JOptionPane.showMessageDialog(null, "Số điện thoại đã tồn tại");
                return false;
            }
        }

        if (e.getEmail() != null && !e.getEmail().isBlank()) {
            if (!e.getEmail().matches(".+@.+\\..+")) {
                JOptionPane.showMessageDialog(null, "Email không hợp lệ");
                return false;
            }
            if (employeeDAO.existsByEmail(e.getEmail())) {
                JOptionPane.showMessageDialog(null, "Email đã tồn tại");
                return false;
            }
        }

        return true;
    }

    public static boolean validateUpdate(Employee e) {
        if (e.getId() == null || e.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID nhân viên không hợp lệ");
            return false;
        }
        return validateCreate(e);
    }
}