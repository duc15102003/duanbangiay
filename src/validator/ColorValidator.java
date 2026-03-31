package validator;
import dao.ColorDAO;
import entity.Color;
import javax.swing.JOptionPane;

public class ColorValidator {

    private static final ColorDAO colorDAO = new ColorDAO();

    public static boolean validateCreate(Color color) {
        if (color.getCode() == null || color.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã màu không được để trống");
            return false;
        }
        if (color.getName() == null || color.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên màu không được để trống");
            return false;
        }
        if (colorDAO.existsByCode(color.getCode(), null)) {
            JOptionPane.showMessageDialog(null, "Mã màu đã tồn tại");
            return false;
        }
        if (colorDAO.existsByName(color.getName(), null)) {
            JOptionPane.showMessageDialog(null, "Tên màu đã tồn tại");
            return false;
        }
        return true;
    }

    public static boolean validateUpdate(Color color) {
        if (color.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID màu không hợp lệ: " + color.getId());
            return false;
        }
        if (color.getCode() == null || color.getCode().isBlank()) {
            JOptionPane.showMessageDialog(null, "Mã màu không được để trống");
            return false;
        }
        if (color.getName() == null || color.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên màu không được để trống");
            return false;
        }
        if (colorDAO.existsByCode(color.getCode(), color.getId())) {
            JOptionPane.showMessageDialog(null, "Mã màu đã tồn tại");
            return false;
        }
        if (colorDAO.existsByName(color.getName(), color.getId())) {
            JOptionPane.showMessageDialog(null, "Tên màu đã tồn tại");
            return false;
        }
        return true;
    }
}