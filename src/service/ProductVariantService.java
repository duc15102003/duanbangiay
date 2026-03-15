package service;

import dao.ProductVariantDAO;
import entity.ProductVariant;
import entity.filter.ProductVariantFilter;
import java.util.List;
import validator.ProductVariantValidator;

public class ProductVariantService {
 
    private ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    private ProductVariantValidator productVariantValidator = new ProductVariantValidator();
    
    public List<ProductVariant> findAll(ProductVariantFilter filter) {
        return productVariantDAO.findAll(filter);
    }

    public ProductVariant findById(int id) {
        return productVariantDAO.findById(id);
    }

    public boolean create(ProductVariant product) {
        productVariantValidator.validateCreate(product);

        return productVariantDAO.insert(product);
    }

    public boolean update(ProductVariant product) {
        productVariantValidator.validateUpdate(product);
        
        return productVariantDAO.update(product);
    }

    public boolean delete(int id) {
        return productVariantDAO.delete(id);
    }
}
