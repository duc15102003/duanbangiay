package service;

import dao.CategoryDAO;
import entity.Category;
import entity.filter.CategoryFilter;
import java.util.List;

public class CategoryService {
    
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    public List<Category> findAll(CategoryFilter filter) {
        return categoryDAO.findAll(filter);
    }

    public Category findById(int id) {
        return categoryDAO.findById(id);
    }

    public boolean insert(Category request) {
        return categoryDAO.insert(request);
    }

    public boolean update(Category request) {
        return categoryDAO.update(request);
    }

    public boolean delete(int id) {
        return categoryDAO.delete(id);
    }
}