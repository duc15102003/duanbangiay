package validator;

import entity.Product;
import javax.swing.JOptionPane;

public class ProductValidator {

    public static boolean validateCreate(Product product) {

        if (product.getCode() == null || product.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã không được để trống");
            return false;
        }

        if (product.getName() == null || product.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên không được để trống");
            return false;
        }

        if (product.getCategoryId() <= 0) {
            JOptionPane.showMessageDialog(null, "Danh mục không hợp lệ");
            return false;
        }

        if (product.getBrandId() <= 0) {
            JOptionPane.showMessageDialog(null, "Thương hiệu không hợp lệ");
            return false;
        }

        if (product.getStatus() == null) {
            JOptionPane.showMessageDialog(null, "Trạng thái không được để trống");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Product product) {

        if (product.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID sản phẩm không hợp lệ: " + product.getId());
            return false;
        }

        return validateCreate(product);
    }
}