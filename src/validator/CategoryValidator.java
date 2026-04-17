package validator;
import dao.CategoryDAO;
import entity.Category;
import javax.swing.JOptionPane;

public class CategoryValidator {

    private static final CategoryDAO categoryDAO = new CategoryDAO();

    public static boolean validateCreate(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên danh mục không được để trống");
            return false;
        }
        if (categoryDAO.existsByCode(category.getCode(), null)) {
            JOptionPane.showMessageDialog(null, "Mã danh mục đã tồn tại");
            return false;
        }
        if (categoryDAO.existsByName(category.getName(), null)) {
            JOptionPane.showMessageDialog(null, "Tên danh mục đã tồn tại");
            return false;
        }
        return true;
    }

    public static boolean validateUpdate(Category category) {
        if (category.getId() <= 0) {
            JOptionPane.showMessageDialog(null, "ID danh mục không hợp lệ: " + category.getId());
            return false;
        }
        if (category.getName() == null || category.getName().isBlank()) {
            JOptionPane.showMessageDialog(null, "Tên danh mục không được để trống");
            return false;
        }
        if (categoryDAO.existsByCode(category.getCode(), category.getId())) {
            JOptionPane.showMessageDialog(null, "Mã danh mục đã tồn tại");
            return false;
        }
        if (categoryDAO.existsByName(category.getName(), category.getId())) {
            JOptionPane.showMessageDialog(null, "Tên danh mục đã tồn tại");
            return false;
        }
        return true;
    }
}