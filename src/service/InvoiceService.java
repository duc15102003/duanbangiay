package service;

import dao.EmployeeDAO;
import dao.InvoiceDAO;
import entity.Invoice;
import entity.Employee;
import enums.OrderStatusEnum;

public class InvoiceService {
    
    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private EmployeeDAO employeeDAO = new EmployeeDAO();
    
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
    
    public boolean updatePaymentInfo(int invoiceId,
                                     Integer employeeId,
                                     Integer customerId,
                                     String customerName,
                                     String customerPhone,
                                     String customerAddress,
                                     String employeeName,
                                     long discountAmount
    ) {
        try {
            Invoice invoice = invoiceDAO.findById(invoiceId);
            if (invoice == null) return false;

            invoice.setEmployeeId(employeeId);
            invoice.setCustomerId(customerId);
            invoice.setCustomerName(customerName);
            invoice.setCustomerPhone(customerPhone);
            invoice.setCustomerAddress(customerAddress);
            invoice.setEmployeeName(employeeName);
            invoice.setDiscountAmount(discountAmount);

            return invoiceDAO.updateInvoicePaymentInfo(invoice);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String findNameById(Integer employeeId) {
        if (employeeId == null) return null;
        Employee emp = employeeDAO.findById(employeeId);
        if (emp != null) {
            return emp.getName();
        }
        return null;
    }
    
    public boolean updatePaymentType(int invoiceId, String paymentType){
        return invoiceDAO.updatePaymentType(invoiceId, paymentType);
    }
}
