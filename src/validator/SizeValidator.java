package validator;

import entity.Size;
import javax.swing.JOptionPane;

public class SizeValidator {
    
    public static boolean validateCreate(Size size) {
        
        if (size.getCode() == null || size.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã kích thước không được để trống");
            return false;
        }
        
        if (size.getName() == null || size.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên kích thước không được để trống");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Size size) {

        if (size.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID không hợp lệ: " + size.getId());
            return false;
        }

        return validateCreate(size);
    }
}