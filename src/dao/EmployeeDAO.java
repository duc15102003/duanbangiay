package dao;

import config.DBConfig;
import entity.Employee;
import entity.filter.EmployeeFilter;
import enums.RoleEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    private DBConfig dbConfig = new DBConfig();

    public List<Employee> findAll(EmployeeFilter filter) {

        List<Employee> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM employee
            WHERE deleted_at IS NULL
                                    
        """);

        List<Object> params = new ArrayList<>();

        if (filter != null && filter.getSearch() != null && !filter.getSearch().isBlank()) {
            sql.append(" AND (name LIKE ? OR phone LIKE ? OR code LIKE ? OR username LIKE ?)");
            String keyword = "%" + filter.getSearch().trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Employee findById(int id) {

        String sql = """
            SELECT *
            FROM employee
            WHERE id = ?
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean insert(Employee request) {

        String sql = """
            INSERT INTO employee
            (code, name, username, password, phone, email, address, role, date_of_birth, gender, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getUsername());
            ps.setString(4, request.getPassword());
            ps.setString(5, request.getPhone());
            ps.setString(6, request.getEmail());
            ps.setString(7, request.getAddress());
            ps.setInt(8, request.getRole().getValue());
            ps.setDate(9, request.getDateOfBirth());
            if(request.getGender() == null){
                ps.setNull(10, java.sql.Types.BOOLEAN);
            }else{
                ps.setBoolean(10, request.getGender());
            }
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean update(Employee request) {

        String sql = """
            UPDATE employee
            SET code = ?,
                name = ?,
                username = ?,
                password = ?,
                phone = ?,
                email = ?,
                address = ?,
                role = ?,
                date_of_birth = ?,
                gender = ?,
                updated_at = ?
            WHERE id = ? AND deleted_at IS NULL
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getUsername());
            ps.setString(4, request.getPassword());
            ps.setString(5, request.getPhone());
            ps.setString(6, request.getEmail());
            ps.setString(7, request.getAddress());
            ps.setInt(8, request.getRole().getValue());
            ps.setDate(9, request.getDateOfBirth());
            ps.setBoolean(10, request.getGender());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(12, request.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean delete(int id) {

        String sql = """
            UPDATE employee
            SET deleted_at = ?
            WHERE id = ?
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Employee mapRow(ResultSet rs) throws Exception {

        Employee e = new Employee();

        e.setId(rs.getInt("id"));
        e.setCode(rs.getString("code"));
        e.setName(rs.getString("name"));
        e.setUsername(rs.getString("username"));
        e.setPassword(rs.getString("password"));
        e.setPhone(rs.getString("phone"));
        e.setEmail(rs.getString("email"));
        e.setAddress(rs.getString("address"));
        e.setRole(RoleEnum.fromValue(rs.getInt("role")));
        e.setDateOfBirth(rs.getDate("date_of_birth"));
        e.setGender(rs.getBoolean("gender"));

        return e;
    }
    
    public boolean existsByCode(String code) {
        String sql = "SELECT 1 FROM employee WHERE LOWER(code) = LOWER(?) AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Kiểm tra tồn tại theo username
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM employee WHERE LOWER(username) = LOWER(?) AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        String sql = "SELECT 1 FROM employee WHERE phone = ? AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) return false;
        String sql = "SELECT 1 FROM employee WHERE LOWER(email) = LOWER(?) AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean existsByCodeExcludeId(String code, int id) {
        String sql = "SELECT 1 FROM employee WHERE LOWER(code) = LOWER(?) AND id <> ? AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            ps.setInt(2, id);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean existsByUsernameExcludeId(String username, int id) {
        String sql = "SELECT 1 FROM employee WHERE LOWER(username) = LOWER(?) AND id <> ? AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            ps.setInt(2, id);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean existsByPhoneExcludeId(String phone, int id) {
        String sql = "SELECT 1 FROM employee WHERE phone = ? AND id <> ? AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone.trim());
            ps.setInt(2, id);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean existsByEmailExcludeId(String email, int id) {
        String sql = "SELECT 1 FROM employee WHERE LOWER(email) = LOWER(?) AND id <> ? AND deleted_at IS NULL";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ps.setInt(2, id);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}