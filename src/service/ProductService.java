package service;

import dao.ProductDAO;
import dao.ProductVariantDAO;
import entity.Product;
import entity.filter.ProductFilter;
import java.util.List;
import validator.ProductValidator;

public class ProductService {
    
    private ProductDAO productDAO = new ProductDAO();
    private ProductVariantDAO productVariantDAO = new ProductVariantDAO();

    public List<Product> findAll(ProductFilter filter) {
        return productDAO.findAll(filter);
    }

    public Product findById(int id) {
        return productDAO.findById(id);
    }

    public boolean create(Product product) {
        if (!ProductValidator.validateCreate(product)) {
            return false;
        }
        
        String nextCode = generateNextCode();
        product.setCode(nextCode);
        
        return productDAO.insert(product);
    }

    public boolean update(Product product) {
        if (!ProductValidator.validateUpdate(product)) {
            return false;
        }
        return productDAO.update(product);
    }

    public boolean delete(int id) {
        return productDAO.delete(id);
    }
    
    public int countVariants(int productId) {
        return productVariantDAO.countByProductId(productId);
    }
    
    private String generateNextCode() {
        String lastCode = productDAO.getLastProductCode();

        if (lastCode == null) {
            return "SP001";
        }

        int number = Integer.parseInt(lastCode.substring(2));
        number++;

        return String.format("SP%03d", number);
    }
}