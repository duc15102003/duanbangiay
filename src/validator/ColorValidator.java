package validator;

import entity.Color;
import javax.swing.JOptionPane;

public class ColorValidator {
    
    public static boolean validateCreate(Color color) {
        
        if (color.getCode() == null || color.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã màu không được để trống");
            return false;
        }
        
        if (color.getName() == null || color.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên màu không được để trống");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Color color) {

        if (color.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID màu không hợp lệ: " + color.getId());
            return false;
        }

        return validateCreate(color);
    }
}