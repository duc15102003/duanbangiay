package service;

import dao.SizeDAO;
import entity.Size;
import entity.filter.SizeFilter;
import java.util.List;
import validator.SizeValidator;

public class SizeService {
    
    private SizeDAO sizeDAO = new SizeDAO();
    
    public List<Size> findAll(SizeFilter filter) {
        return sizeDAO.findAll(filter);
    }
    
    public Size findById(int id) {
        return sizeDAO.findById(id);
    }

    public boolean insert(Size request) {
        if (!SizeValidator.validateCreate(request)) {
            return false;
        }
        request.setCode(generateCode());
        return sizeDAO.insert(request);
    }

    public boolean update(Size request) {
        if (!SizeValidator.validateUpdate(request)) {
            return false;
        }
        return sizeDAO.update(request);
    }

    public boolean delete(int id) {
        return sizeDAO.delete(id);
    }
    
    public String generateCode() {

        String maxCode = sizeDAO.getMaxCode();

        if (maxCode == null) {
            return "SZ001";
        }

        int number = Integer.parseInt(maxCode.replace("SZ", ""));
        number++;

        return "SZ" + String.format("%03d", number);
    }
}