package service;

import dao.BrandDAO;
import entity.Brand;
import entity.filter.BrandFilter;
import java.util.List;

public class BrandService {
    
    private BrandDAO brandDAO = new BrandDAO();
    
    public List<Brand> findAll(BrandFilter filter) {
        return brandDAO.findAll(filter);
    }  
    
    public Brand findById(int id) {
        return brandDAO.findById(id);
    }

    public boolean insert(Brand request) {
        return brandDAO.insert(request);
    }

    public boolean update(Brand request) {
        return brandDAO.update(request);
    }

    public boolean delete(int id) {
        return brandDAO.delete(id);
    }
}
