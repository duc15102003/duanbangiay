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
        request.setCode(generateCode());
        return customerDAO.insert(request);
    }

    public boolean update(Customer request) {
        if (!CustomerValidator.validateUpdate(request)) {
            return false;
        }
        return customerDAO.update(request);
    }

    public boolean delete(int id, int currentUserId) {
        return customerDAO.delete(id, currentUserId);
    }
    
    public String generateCode() {

        String maxCode = customerDAO.getMaxCode();

        if (maxCode == null) {
            return "CTM001";
        }

        int number = Integer.parseInt(maxCode.replace("CTM", ""));
        number++;

        return "CTM" + String.format("%03d", number);
    }
}