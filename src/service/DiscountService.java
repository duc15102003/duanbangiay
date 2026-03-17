package service;

import dao.DiscountDAO;
import dao.InvoiceDAO;
import entity.Discount;
import entity.filter.DiscountFilter;

import java.util.List;

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

        if (request == null) {
            return false;
        }

        return discountDAO.insert(request);
    }

    public boolean update(Discount request) {

        if (request == null || request.getId() == 0) {
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

        Discount discount = discountDAO.checkDiscount(code);

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
}