package validator;

import dao.EmployeeDAO;
import entity.Employee;
import javax.swing.JOptionPane;

public class EmployeeValidator {

    private static EmployeeDAO employeeDAO = new EmployeeDAO();

    public static boolean validateCreate(Employee e) {
        if (e == null) {
            JOptionPane.showMessageDialog(null, "Dữ liệu không hợp lệ");
            return false;
        }

        // ===== CODE =====
        if (e.getCode() == null || e.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã nhân viên không được để trống");
            return false;
        }
        if (employeeDAO.existsByCode(e.getCode())) {
            JOptionPane.showMessageDialog(null, "Mã nhân viên đã tồn tại");
            return false;
        }

        // ===== NAME =====
        if (e.getName() == null || e.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên nhân viên không được để trống");
            return false;
        }
        
        if (!e.getName().matches("[a-zA-Z\\sÀ-ỹ]+")) {
            JOptionPane.showMessageDialog(null, "Tên nhân viên chỉ được chứa chữ cái và khoảng trắng");
            return false;
        }

        // ===== USERNAME =====
        if (e.getUsername() == null || e.getUsername().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên đăng nhập không được để trống");
            return false;
        }
        if (employeeDAO.existsByUsername(e.getUsername())) {
            JOptionPane.showMessageDialog(null, "Tên đăng nhập đã tồn tại");
            return false;
        }

        // ===== PASSWORD =====
        if (e.getPassword() == null || e.getPassword().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mật khẩu không được để trống");
            return false;
        }

        // ===== ROLE =====
        if (e.getRole() == null) {
            JOptionPane.showMessageDialog(null, "Vai trò không được để trống");
            return false;
        }

        // ===== PHONE =====
        if (e.getPhone() == null || e.getPhone().isBlank()) {
            JOptionPane.showMessageDialog(null, "Số điện thoại không được để trống");
            return false;
        }
        if (!e.getPhone().matches("0\\d{9}")) {
            JOptionPane.showMessageDialog(null, "Số điện thoại không hợp lệ (bắt đầu bằng 0 và đủ 10 số)");
            return false;
        }
        if (employeeDAO.existsByPhone(e.getPhone())) {
            JOptionPane.showMessageDialog(null, "Số điện thoại đã tồn tại");
            return false;
        }

        // ===== EMAIL =====
        if (e.getEmail() == null || e.getEmail().isBlank()) {
            JOptionPane.showMessageDialog(null, "Email không được để trống");
            return false;
        }
        if (!e.getEmail().matches(".+@.+\\..+")) {
            JOptionPane.showMessageDialog(null, "Email không hợp lệ");
            return false;
        }
        if (employeeDAO.existsByEmail(e.getEmail())) {
            JOptionPane.showMessageDialog(null, "Email đã tồn tại");
            return false;
        }
        
        // ===== DATE OF BIRTH =====
        if (e.getDateOfBirth() == null) {
            JOptionPane.showMessageDialog(null, "Ngày sinh phải được chọn");
            return false;
        }

        java.util.Date today = new java.util.Date();

        if (e.getDateOfBirth().after(today)) {
            JOptionPane.showMessageDialog(null, "Ngày sinh không được lớn hơn ngày hiện tại");
            return false;
        }

        long diff = today.getTime() - e.getDateOfBirth().getTime();
        long age = diff / (1000L * 60 * 60 * 24 * 365);

        if (age < 0) {
            JOptionPane.showMessageDialog(null, "Ngày sinh không hợp lệ");
            return false;
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