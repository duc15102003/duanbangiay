package validator;
import dao.SizeDAO;
import entity.Size;
import javax.swing.JOptionPane;

public class SizeValidator {

    private static final SizeDAO sizeDAO = new SizeDAO();

    public static boolean validateCreate(Size size) {

        if (size.getName() == null || size.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên kích thước không được để trống");
            return false;
        }
        if (sizeDAO.existsByCode(size.getCode(), null)) {
            JOptionPane.showMessageDialog(null, "Mã kích thước đã tồn tại");
            return false;
        }
        if (sizeDAO.existsByName(size.getName(), null)) {
            JOptionPane.showMessageDialog(null, "Tên kích thước đã tồn tại");
            return false;
        }
        return true;
    }

    public static boolean validateUpdate(Size size) {
        if (size.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID không hợp lệ: " + size.getId());
            return false;
        }

        if (size.getName() == null || size.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên kích thước không được để trống");
            return false;
        }
        if (sizeDAO.existsByCode(size.getCode(), size.getId())) {
            JOptionPane.showMessageDialog(null, "Mã kích thước đã tồn tại");
            return false;
        }
        if (sizeDAO.existsByName(size.getName(), size.getId())) {
            JOptionPane.showMessageDialog(null, "Tên kích thước đã tồn tại");
            return false;
        }
        return true;
    }
}