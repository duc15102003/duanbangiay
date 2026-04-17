package service;

import dao.CartDAO;
import dao.ProductVariantDAO;
import entity.Invoice;
import entity.InvoiceItem;
import entity.ProductVariant;
import entity.filter.InvoiceFilter;
import java.util.List;

public class CartService {
    
    private CartDAO cartDAO = new CartDAO();
    private ProductVariantService productVariantService = new ProductVariantService();
    private ProductVariantDAO productVariantDAO = new ProductVariantDAO();
    
    public List<Invoice> findAll(InvoiceFilter filter, Integer employeeId){
        return cartDAO.findAll(filter, employeeId);
    }
    
    public List<InvoiceItem> findByInvoiceId(int invoiceId, String search) {
        return cartDAO.findByInvoiceId(invoiceId, search);
    }
    
    public boolean updateQuantity(int invoiceId, int productVariantId, int newQty) {

        InvoiceItem item = cartDAO.findItem(invoiceId, productVariantId);
        if (item == null) return false;

        int oldQty = item.getQuantity();
        int diff = newQty - oldQty;

        boolean success = true;

        if (diff > 0) {
            success = productVariantDAO.decreaseStock(productVariantId, diff);
        } else if (diff < 0) {
            productVariantDAO.increaseStock(productVariantId, -diff);
        }

        if (!success) return false;

        boolean updated = cartDAO.updateQuantity(invoiceId, productVariantId, newQty);

        if (!updated && diff > 0) {
            productVariantDAO.increaseStock(productVariantId, diff);
        }

        return updated;
    }
    
    public boolean deleteRowCart(int invoiceId, int productVariantId, int quantity) {

        productVariantDAO.increaseStock(productVariantId, quantity);

        boolean deleted = cartDAO.deleteRowCart(invoiceId, productVariantId);

        if (!deleted) {
            productVariantDAO.decreaseStock(productVariantId, quantity);
        }

        return deleted;
    }
    
    public boolean addProductToCart(int invoiceId, int productVariantId, int quantity, float price) {

        boolean success = productVariantDAO.decreaseStock(productVariantId, quantity);

        if (!success) {
            return false;
        };

        boolean added = cartDAO.addProductToCart(invoiceId, productVariantId, quantity, price);

        if (!added) {
            productVariantDAO.increaseStock(productVariantId, quantity);
            return false;
        }

        return true;
    }
    
    public boolean validateProductStatus(List<ProductVariant> list){
        return cartDAO.validateProductStatus(list);
    }
}
