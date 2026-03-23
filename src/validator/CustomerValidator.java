package validator;

import dao.CustomerDAO;
import entity.Customer;
import javax.swing.JOptionPane;

public class CustomerValidator {

    private static CustomerDAO customerDAO = new CustomerDAO();

    public static boolean validateCreate(Customer c) {
        if (c == null) {
            JOptionPane.showMessageDialog(null, "Dữ liệu không hợp lệ");
            return false;
        }

        // ===== CODE =====
        if (c.getCode() == null || c.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã khách hàng không được để trống");
            return false;
        }
        if (customerDAO.existsByCode(c.getCode())) {
            JOptionPane.showMessageDialog(null, "Mã khách hàng đã tồn tại");
            return false;
        }

        // ===== NAME =====
        if (c.getName() == null || c.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên khách hàng không được để trống");
            return false;
        }
        
        if (!c.getName().matches("[a-zA-Z\\sÀ-ỹ]+")) {
            JOptionPane.showMessageDialog(null, "Tên khách hàng chỉ được chứa chữ cái và khoảng trắng");
            return false;
        }

        // ===== PHONE =====
        if (c.getPhone() == null || c.getPhone().isBlank()) {
            JOptionPane.showMessageDialog(null, "Số điện thoại không được để trống");
            return false;
        }
        if (!c.getPhone().matches("0\\d{9}")) {
            JOptionPane.showMessageDialog(null, "Số điện thoại không hợp lệ (phải bắt đầu 0 và đủ 10 số)");
            return false;
        }
        if (customerDAO.existsByPhone(c.getPhone())) {
            JOptionPane.showMessageDialog(null, "Số điện thoại đã tồn tại");
            return false;
        }

        // ===== EMAIL =====
        if (c.getEmail() == null || c.getEmail().isBlank()) {
            JOptionPane.showMessageDialog(null, "Email không được để trống");
            return false;
        }
        if (!c.getEmail().matches(".+@.+\\..+")) {
            JOptionPane.showMessageDialog(null, "Email không hợp lệ");
            return false;
        }
        if (customerDAO.existsByEmail(c.getEmail())) {
            JOptionPane.showMessageDialog(null, "Email đã tồn tại");
            return false;
        }

        // ===== DATE OF BIRTH =====
        if (c.getDateOfBirth() == null) {
            JOptionPane.showMessageDialog(null, "Ngày sinh phải được chọn");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Customer c) {
        if (c.getId() == null || c.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID khách hàng không hợp lệ");
            return false;
        }

        return validateCreate(c);
    }
}