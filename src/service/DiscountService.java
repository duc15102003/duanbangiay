package service;

import dao.DiscountDAO;
import entity.Discount;
import entity.filter.DiscountFilter;

import java.util.List;

public class DiscountService {

    private DiscountDAO discountDAO = new DiscountDAO();

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
}