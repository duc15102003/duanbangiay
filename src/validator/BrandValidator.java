package validator;

import entity.Brand;
import javax.swing.JOptionPane;

public class BrandValidator {
    
    public static boolean validateCreate(Brand brand) {
        
        if (brand.getCode() == null || brand.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã thương hiệu không được để trống");
            return false;
        }
        
        if (brand.getName() == null || brand.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên thương hiệu không được để trống");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Brand brand) {

        if (brand.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID thương hiệu không hợp lệ: " + brand.getId());
            return false;
        }

        return validateCreate(brand);
    }
}