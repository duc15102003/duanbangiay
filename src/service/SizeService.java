package service;

import dao.SizeDAO;
import entity.Size;
import entity.filter.SizeFilter;
import java.util.List;

public class SizeService {
    
    private SizeDAO sizeDAO = new SizeDAO();
    
    public List<Size> findAll(SizeFilter filter) {
        return sizeDAO.findAll(filter);
    }
    
    public Size findById(int id) {
        return sizeDAO.findById(id);
    }

    public boolean insert(Size request) {
        return sizeDAO.insert(request);
    }

    public boolean update(Size request) {
        return sizeDAO.update(request);
    }

    public boolean delete(int id) {
        return sizeDAO.delete(id);
    }
}