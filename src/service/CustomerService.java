package service;

import dao.CustomerDAO;
import entity.Customer;
import entity.filter.CustomerFilter;
import java.util.List;

public class CustomerService {

    private CustomerDAO customerDAO = new CustomerDAO();

    public List<Customer> findAll(CustomerFilter filter) {
        return customerDAO.findAll(filter);
    }

    public Customer findById(int id) {
        return customerDAO.findById(id);
    }

    public boolean insert(Customer request) {
        return customerDAO.insert(request);
    }

    public boolean update(Customer request) {
        return customerDAO.update(request);
    }

    public boolean delete(int id) {
        return customerDAO.delete(id);
    }
}