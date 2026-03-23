package service;

import dao.CustomerDAO;
import entity.Customer;
import entity.filter.CustomerFilter;
import java.util.List;
import validator.CustomerValidator;

public class CustomerService {

    private CustomerDAO customerDAO = new CustomerDAO();

    public List<Customer> findAll(CustomerFilter filter) {
        return customerDAO.findAll(filter);
    }

    public Customer findById(int id) {
        return customerDAO.findById(id);
    }

    public boolean insert(Customer request) {
        if (!CustomerValidator.validateCreate(request)) {
            return false;
        }
        return customerDAO.insert(request);
    }

    public boolean update(Customer request) {
        if (!CustomerValidator.validateUpdate(request)) {
            return false;
        }
        return customerDAO.update(request);
    }

    public boolean delete(int id) {
        return customerDAO.delete(id);
    }
}