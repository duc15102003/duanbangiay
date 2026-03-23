package validator;

import entity.Category;
import javax.swing.JOptionPane;

public class CategoryValidator {
    
    public static boolean validateCreate(Category category) {
        
        if (category.getCode() == null || category.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã danh mục không được để trống");
            return false;
        }
        
        if (category.getName() == null || category.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên danh mục không được để trống");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Category category) {

        if (category.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID danh mục không hợp lệ: " + category.getId());
            return false;
        }

        return validateCreate(category);
    }
}