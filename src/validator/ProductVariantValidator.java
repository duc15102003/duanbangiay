package validator;

import entity.ProductVariant;
import javax.swing.JOptionPane;

public class ProductVariantValidator {
    
    public static boolean validateCreate(ProductVariant pv) {

        if (pv.getProductId() <= 0) {
            JOptionPane.showMessageDialog(null, "Sản phẩm không hợp lệ");
            return false;
        }

        if (pv.getColorId() <= 0) {
            JOptionPane.showMessageDialog(null, "Màu sắc không hợp lệ");
            return false;
        }

        if (pv.getSizeId() <= 0) {
            JOptionPane.showMessageDialog(null, "Kích thước không hợp lệ");
            return false;
        }

        if (pv.getPrice() <= 0) {
            JOptionPane.showMessageDialog(null, "Giá phải lớn hơn 0");
            return false;
        }

        if (pv.getQuantity() < 0) {
            JOptionPane.showMessageDialog(null, "Số lượng không hợp lệ");
            return false;
        }

        if (pv.getImage() == null || pv.getImage().isBlank()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập đường dẫn hình ảnh");
            return false;
        }
        
        return true;
    }

    public static boolean validateUpdate(ProductVariant pv) {

        if (pv.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID không hợp lệ: " + pv.getId());
            return false;
        }

        return validateCreate(pv);
    }
}