package service;

import dao.DiscountDAO;
import dao.InvoiceDAO;
import entity.Discount;
import entity.filter.DiscountFilter;

import java.util.List;
import validator.DiscountValidator;

public class DiscountService {

    private DiscountDAO discountDAO = new DiscountDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();

    public List<Discount> findAll(DiscountFilter filter) {
        return discountDAO.findAll(filter);
    }

    public Discount findById(int id) {
        return discountDAO.findById(id);
    }

    public boolean insert(Discount request) {

        if (!DiscountValidator.validateCreate(request)) {
            return false;
        }

        request.setCode(generateCode());
        return discountDAO.insert(request);
    }

    public boolean update(Discount request) {

        if (!DiscountValidator.validateUpdate(request)) {
            return false;
        }

        return discountDAO.update(request);
    }

    public boolean delete(int id) {

        if (id <= 0) {
            return false;
        }

        return discountDAO.delete(id);
    }
    
    public Discount checkDiscount(String code, int invoiceId, float total, Integer customerId) {

        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        Discount discount = discountDAO.checkDiscount(code, total);

        if (discount == null) {
            return null;
        }

        float discountAmount = 0;

        // ===== TÍNH GIẢM GIÁ =====
        if ("%".equals(discount.getDiscountType())) {

            discountAmount = total * discount.getDiscountValue() / 100f;

            if (discount.getMaximumDiscount() != null) {
                discountAmount = Math.min(discountAmount, discount.getMaximumDiscount());
            }

        } else if ("Tiền mặt".equalsIgnoreCase(discount.getDiscountType())) {

            discountAmount = discount.getDiscountValue();
        }

        if (discountAmount > total) {
            discountAmount = total;
        }

        float finalTotal = total - discountAmount;

        invoiceDAO.updateDiscount(invoiceId, discount.getId(), finalTotal, customerId);

        return discount;
    }
    
    public boolean useDiscount(String code){
        return discountDAO.useDiscount(code);
    }
    
    public boolean updateStatus(int id, int status) {
        return discountDAO.updateStatus(id, status);
    }
    
    public List<Discount> getValidDiscounts(double orderTotal) {
        return discountDAO.getValidDiscounts(orderTotal);
    }
    
    public Discount checkDiscountValid(String code, double orderTotal){
        return discountDAO.checkDiscountValid(code, orderTotal);
    }
    
    public String generateCode() {

        String maxCode = discountDAO.getMaxCode();

        if (maxCode == null) {
            return "DC001";
        }

        int number = Integer.parseInt(maxCode.replace("DC", ""));
        number++;

        return "DC" + String.format("%03d", number);
    }
}