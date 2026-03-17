package service;

import dao.CartDAO;
import entity.Invoice;
import entity.InvoiceItem;
import entity.filter.InvoiceFilter;
import java.util.List;

public class CartService {
    
    private CartDAO cartDAO = new CartDAO();
    
    public List<Invoice> findAll(InvoiceFilter filter, Integer employeeId){
        return cartDAO.findAll(filter, employeeId);
    }
    
    public List<InvoiceItem> findByInvoiceId(int invoiceId, String search) {
        return cartDAO.findByInvoiceId(invoiceId, search);
    }
    
    public boolean updateQuantity(int invoiceId, int productVariantId, int quantity) {
        return cartDAO.updateQuantity(invoiceId, productVariantId, quantity);
    }
    
    public boolean deleteRowCart(int invoiceId, int productVariantId){
        return cartDAO.deleteRowCart(invoiceId, productVariantId);
    }
    
    public boolean addProductToCart(int invoiceId, int productVariantId, int quantity, float price){
        return cartDAO.addProductToCart(invoiceId, productVariantId, quantity, price);
    }
}
