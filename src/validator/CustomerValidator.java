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

        if (c.getCode() == null || c.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã khách hàng không được để trống");
            return false;
        }
        if (customerDAO.existsByCode(c.getCode())) {
            JOptionPane.showMessageDialog(null, "Mã khách hàng đã tồn tại");
            return false;
        }

        if (c.getName() == null || c.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên khách hàng không được để trống");
            return false;
        }

        if (c.getPhone() != null && !c.getPhone().isBlank()) {
            if (!c.getPhone().matches("\\d+")) {
                JOptionPane.showMessageDialog(null, "Số điện thoại không hợp lệ");
                return false;
            }
            if (customerDAO.existsByPhone(c.getPhone())) {
                JOptionPane.showMessageDialog(null, "Số điện thoại đã tồn tại");
                return false;
            }
        }

        if (c.getEmail() != null && !c.getEmail().isBlank()) {
            if (!c.getEmail().matches(".+@.+\\..+")) {
                JOptionPane.showMessageDialog(null, "Email không hợp lệ");
                return false;
            }
            if (customerDAO.existsByEmail(c.getEmail())) {
                JOptionPane.showMessageDialog(null, "Email đã tồn tại");
                return false;
            }
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