package service;

import dao.AuthDAO;
import entity.AuthResult;

public class AuthService {
    
    private AuthDAO authDAO = new AuthDAO();
    
    public AuthResult login(String account, String password) {
        return authDAO.login(account, password);
    }
    
    public boolean register(String username, String phone, String email, String password, String confirmPassword){
        return register(username, phone, email, password, confirmPassword);
    }
}
