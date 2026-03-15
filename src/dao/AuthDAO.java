package dao;

import config.DBConfig;
import enums.RoleEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import entity.AuthResult;
import javax.swing.JOptionPane;

public class AuthDAO {
    
    private DBConfig dbConfig = new DBConfig();
    
    public AuthResult login(String account, String password) {

        String sql = """
                     SELECT id, name, role
                     FROM employee
                     WHERE (username = ? OR phone = ? OR email = ?)
                     AND password = ?
                     AND deleted_at IS NULL
                     """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, account);
            ps.setString(2, account);
            ps.setString(3, account);
            ps.setString(4, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Integer id = rs.getInt("id");
                String name = rs.getString("name");
                int roleValue = rs.getInt("role");
                RoleEnum role = RoleEnum.fromValue(roleValue);
                
                return new AuthResult(id, name, role);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public boolean register(String account, String password, String confirmPassword){

        if(account == null || account.trim().isEmpty()){
            JOptionPane.showMessageDialog(null,"Không được để trống tài khoản");
            return false;
        }

        if(password == null || password.length() < 6){
            JOptionPane.showMessageDialog(null,"Password phải >= 6 ký tự");
            return false;
        }

        if(!password.equals(confirmPassword)){
            JOptionPane.showMessageDialog(null,"Mật khẩu xác nhận không khớp");
            return false;
        }

        String username = null;
        String phone = null;
        String email = null;

        account = account.trim();

        // check email
        if(account.contains("@")){
            if(!account.matches("^[A-Za-z0-9+_.-]+@(.+)$")){
                JOptionPane.showMessageDialog(null,"Email không hợp lệ");
                return false;
            }
            email = account;
        }

        // check phone
        else if(account.matches("^0\\d{9,10}$")){
            phone = account;
        }

        // username thường
        else{
            if(!account.matches("^[a-zA-Z0-9_]+$")){
                JOptionPane.showMessageDialog(null,"Username không được chứa ký tự đặc biệt");
                return false;
            }
            username = account;
        }

        if(existsUser(username, phone, email)){
            JOptionPane.showMessageDialog(null,"Tài khoản đã tồn tại");
            return false;
        }

        String sql = """
            INSERT INTO employee(username, phone, email, password, created_at, role)
            VALUES(?,?,?,?,GETDATE(), 1)
        """;

        try(Connection con = DBConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){

            ps.setString(1, username);
            ps.setString(2, phone);
            ps.setString(3, email);
            ps.setString(4, password);

            int rows = ps.executeUpdate();

            if(rows > 0){
                JOptionPane.showMessageDialog(null,"Đăng ký thành công");
                return true;
            }

        }catch(Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,"Đăng ký thất bại");
        }

        return false;
    }
    
    public boolean existsUser(String username, String phone, String email){

        String sql = """
            SELECT 1
            FROM employee
            WHERE username = ?
               OR phone = ?
               OR email = ?
        """;

        try(Connection con = DBConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)){

            ps.setString(1, username);
            ps.setString(2, phone);
            ps.setString(3, email);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        }catch(Exception e){
            e.printStackTrace();
        }

        return false;
    }
}
