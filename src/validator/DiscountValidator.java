package validator;

import dao.DiscountDAO;
import entity.Discount;
import javax.swing.JOptionPane;

public class DiscountValidator {
    
    private static final DiscountDAO discountDAO = new DiscountDAO();
    
    public static boolean validateCreate(Discount d) {

        if (d == null) {
            JOptionPane.showMessageDialog(null, "Dữ liệu không hợp lệ");
            return false;
        }

        if (d.getCode() == null || d.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã giảm giá không được để trống");
            return false;
        }
        
        if(discountDAO.existsByCode(d.getCode(), null)){
            JOptionPane.showMessageDialog(null, "Mã giảm giá đã tồn tại!");
            return false;
        }

        if (d.getDiscountType() == null || d.getDiscountType().isBlank()) {
            JOptionPane.showMessageDialog(null, "Loại giảm giá không hợp lệ");
            return false;
        }

        // Chỉ cho phép 2 loại
        if (!d.getDiscountType().equals("%") 
                && !d.getDiscountType().equalsIgnoreCase("Tiền mặt")) {
            JOptionPane.showMessageDialog(null, "Loại giảm giá phải là % hoặc Tiền mặt");
            return false;
        }

        if (d.getDiscountValue() <= 0) {
            JOptionPane.showMessageDialog(null, "Giá trị giảm phải lớn hơn 0");
            return false;
        }

        // Nếu là % thì không quá 100
        if ("%".equals(d.getDiscountType()) && d.getDiscountValue() > 100) {
            JOptionPane.showMessageDialog(null, "Giảm giá % không được vượt quá 100%");
            return false;
        }

        // maximumDiscount nếu có thì phải > 0
        if (d.getMaximumDiscount() != null && d.getMaximumDiscount() <= 0) {
            JOptionPane.showMessageDialog(null, "Giảm tối đa phải lớn hơn 0");
            return false;
        }

        // Validate ngày
        if (d.getStartedAt() == null || d.getEndedAt() == null) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu và kết thúc không được để trống");
            return false;
        }

        if (d.getStartedAt().isAfter(d.getEndedAt())) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu phải trước ngày kết thúc");
            return false;
        }

        if (d.getStatus() == null) {
            JOptionPane.showMessageDialog(null, "Trạng thái không được để trống");
            return false;
        }

        return true;
    }

    public static boolean validateUpdate(Discount d) {

        if (d.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID giảm giá không hợp lệ: " + d.getId());
            return false;
        }

        if (d.getCode() == null || d.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã giảm giá không được để trống");
            return false;
        }

        if (discountDAO.existsByCode(d.getCode(), d.getId())) {
            JOptionPane.showMessageDialog(null, "Mã giảm giá đã tồn tại!");
            return false;
        }

        if (d.getDiscountType() == null || d.getDiscountType().isBlank()) {
            JOptionPane.showMessageDialog(null, "Loại giảm giá không hợp lệ");
            return false;
        }

        if (!d.getDiscountType().equals("%") 
                && !d.getDiscountType().equalsIgnoreCase("Tiền mặt")) {
            JOptionPane.showMessageDialog(null, "Loại giảm giá phải là % hoặc Tiền mặt");
            return false;
        }

        if (d.getDiscountValue() <= 0) {
            JOptionPane.showMessageDialog(null, "Giá trị giảm phải lớn hơn 0");
            return false;
        }

        if ("%".equals(d.getDiscountType()) && d.getDiscountValue() > 100) {
            JOptionPane.showMessageDialog(null, "Giảm giá % không được vượt quá 100%");
            return false;
        }

        if (d.getMaximumDiscount() != null && d.getMaximumDiscount() <= 0) {
            JOptionPane.showMessageDialog(null, "Giảm tối đa phải lớn hơn 0");
            return false;
        }

        if (d.getStartedAt() == null || d.getEndedAt() == null) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu và kết thúc không được để trống");
            return false;
        }

        if (d.getStartedAt().isAfter(d.getEndedAt())) {
            JOptionPane.showMessageDialog(null, "Ngày bắt đầu phải trước ngày kết thúc");
            return false;
        }

        if (d.getStatus() == null) {
            JOptionPane.showMessageDialog(null, "Trạng thái không được để trống");
            return false;
        }

        return true;
    }
}