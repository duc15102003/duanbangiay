package service;

import dao.InvoiceDAO;
import entity.Invoice;

public class InvoiceService {
    
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    
    public boolean insert(Invoice invoice){
        return invoiceDAO.insert(invoice);
    }
    
    public boolean delete(int invoiceId){
         return invoiceDAO.delete(invoiceId);
    }
}
