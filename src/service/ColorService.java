package service;

import dao.ColorDAO;
import entity.Color;
import entity.filter.ColorFilter;
import java.util.List;

public class ColorService {
    
    private ColorDAO colorDAO = new ColorDAO();
    
    public List<Color> findAll(ColorFilter filter) {
        return colorDAO.findAll(filter);
    }
    
    public Color findById(int id) {
        return colorDAO.findById(id);
    }

    public boolean insert(Color request) {
        return colorDAO.insert(request);
    }

    public boolean update(Color request) {
        return colorDAO.update(request);
    }

    public boolean delete(int id) {
        return colorDAO.delete(id);
    }
}