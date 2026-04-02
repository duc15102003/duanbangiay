package service;

import dao.ProductVariantDAO;
import entity.ProductVariant;
import entity.filter.ProductVariantFilter;
import java.util.List;
import validator.ProductVariantValidator;

public class ProductVariantService {
 
    private ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    
    public List<ProductVariant> findAll(ProductVariantFilter filter) {
        return productVariantDAO.findAll(filter);
    }

    public ProductVariant findById(int id) {
        return productVariantDAO.findById(id);
    }

    public boolean create(ProductVariant product) {
        if (!ProductVariantValidator.validateCreate(product)) {
            return false;
        }
        return productVariantDAO.insert(product);
    }

    public boolean update(ProductVariant product) {
        if (!ProductVariantValidator.validateUpdate(product)) {
            return false;
        }
        return productVariantDAO.update(product);
    }

    public boolean delete(int id) {
        return productVariantDAO.delete(id);
    }
    
    public void updateStockAfterPayment(int invoiceId) {
        productVariantDAO.updateStockAfterPayment(invoiceId);
    }
    
    public boolean checkStockBeforePayment(int invoiceId) {
        return productVariantDAO.checkStockBeforePayment(invoiceId);
    }
    
    public boolean updateQuantity(int id, int newQuantity) {
        return productVariantDAO.updateQuantity(id, newQuantity);
    }
    
    public boolean increaseStock(int productVariantId, int quantity){
        return productVariantDAO.increaseStock(productVariantId, quantity);
    }
    
    public List<ProductVariant> getTop3BestSeller() {
        return productVariantDAO.getTop3BestSeller();
    }
}