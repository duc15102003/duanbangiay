package validator;

import dao.BrandDAO;
import entity.Brand;
import javax.swing.JOptionPane;

public class BrandValidator {
    
    private static final BrandDAO brandDAO = new BrandDAO();
    
    public static boolean validateCreate(Brand brand) {
        
        if (brand.getCode() == null || brand.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã thương hiệu không được để trống");
            return false;
        }
        
        if (brand.getName() == null || brand.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên thương hiệu không được để trống");
            return false;
        }
        
        if (brandDAO.existsByCode(brand.getCode(), null)) {
            JOptionPane.showMessageDialog(null, "Mã thương hiệu đã tồn tại");
            return false;
        }
        if (brandDAO.existsByName(brand.getName(), null)) {
            JOptionPane.showMessageDialog(null, "Tên thương hiệu đã tồn tại");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Brand brand) {

        if (brand.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID thương hiệu không hợp lệ: " + brand.getId());
            return false;
        }
        
        if (brandDAO.existsByCode(brand.getCode(), brand.getId())) {
            JOptionPane.showMessageDialog(null, "Mã thương hiệu đã tồn tại");
            return false;
        }
        if (brandDAO.existsByName(brand.getName(), brand.getId())) {
            JOptionPane.showMessageDialog(null, "Tên thương hiệu đã tồn tại");
            return false;
        }

        return validateCreate(brand);
    }
}