package service;

import dao.ProductDAO;
import entity.Product;
import entity.filter.ProductFilter;
import java.util.List;
import validator.ProductValidator;

public class ProductService {
    
    private ProductDAO productDAO = new ProductDAO();
    private ProductValidator productValidator = new ProductValidator();

    public List<Product> findAll(ProductFilter filter) {
        return productDAO.findAll(filter);
    }

    public Product findById(int id) {
        return productDAO.findById(id);
    }

    public boolean create(Product product) {
        productValidator.validateCreate(product);

        return productDAO.insert(product);
    }

    public boolean update(Product product) {
        productValidator.validateUpdate(product);
        
        return productDAO.update(product);
    }

    public boolean delete(int id) {
        return productDAO.delete(id);
    }
}
