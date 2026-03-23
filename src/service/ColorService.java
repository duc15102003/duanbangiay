package service;

import dao.ColorDAO;
import entity.Color;
import entity.filter.ColorFilter;
import java.util.List;
import validator.ColorValidator;

public class ColorService {
    
    private ColorDAO colorDAO = new ColorDAO();
    private ColorValidator colorValidator = new ColorValidator();
    
    public List<Color> findAll(ColorFilter filter) {
        return colorDAO.findAll(filter);
    }
    
    public Color findById(int id) {
        return colorDAO.findById(id);
    }

    public boolean insert(Color request) {
        if (!ColorValidator.validateCreate(request)) {
            return false;
        }
        return colorDAO.insert(request);
    }

    public boolean update(Color request) {
        if (!ColorValidator.validateUpdate(request)) {
            return false;
        }
        return colorDAO.update(request);
    }

    public boolean delete(int id) {
        return colorDAO.delete(id);
    }
}