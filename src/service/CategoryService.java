package service;

import dao.CategoryDAO;
import entity.Category;
import entity.filter.CategoryFilter;
import java.util.List;
import validator.CategoryValidator;

public class CategoryService {
    
    private CategoryDAO categoryDAO = new CategoryDAO();
    private CategoryValidator categoryValidator = new CategoryValidator();
    
    public List<Category> findAll(CategoryFilter filter) {
        return categoryDAO.findAll(filter);
    }

    public Category findById(int id) {
        return categoryDAO.findById(id);
    }

    public boolean insert(Category request) {
        if (!CategoryValidator.validateCreate(request)) {
            return false;
        }
        request.setCode(generateCode());
        return categoryDAO.insert(request);
    }

    public boolean update(Category request) {
        if (!CategoryValidator.validateUpdate(request)) {
            return false;
        }
        return categoryDAO.update(request);
    }

    public boolean delete(int id) {
        return categoryDAO.delete(id);
    }
    
    public String generateCode() {

        String maxCode = categoryDAO.getMaxCode();

        if (maxCode == null) {
            return "CG001";
        }

        int number = Integer.parseInt(maxCode.replace("CG", ""));
        number++;

        return "CG" + String.format("%03d", number);
    }
}