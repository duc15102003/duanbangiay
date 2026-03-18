package service;

import dao.InvoiceDAO;
import entity.Invoice;
import enums.OrderStatusEnum;

public class InvoiceService {
    
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    
    public Invoice findById(int id){
        return invoiceDAO.findById(id);
    }
    
    public boolean insert(Invoice invoice){
        return invoiceDAO.insert(invoice);
    }
    
    public boolean delete(int invoiceId){
         return invoiceDAO.delete(invoiceId);
    }
    
    public boolean updateStatus(int invoiceId, OrderStatusEnum status){
        return invoiceDAO.updateStatus(invoiceId, status);
    }
    
     public float getTotalAmount(int invoiceId){
         return invoiceDAO.getTotalAmount(invoiceId);
     }
     
    public boolean updateTotalAmount(int invoiceId, float totalAmount) {
        return invoiceDAO.updateTotalAmount(invoiceId, totalAmount);
    }
}
