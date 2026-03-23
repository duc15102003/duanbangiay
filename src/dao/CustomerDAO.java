package dao;

import config.DBConfig;
import entity.Customer;
import entity.filter.CustomerFilter;
import enums.CustomerStatusEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO implements GenericDAO<Customer, CustomerFilter> {
    
    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Customer> findAll(CustomerFilter filter) {

        List<Customer> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                c.*
            FROM customer c
            WHERE c.deleted_at IS NULL
        """);

        List<Object> params = new ArrayList<>();

        if (filter != null) {

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                sql.append(" AND (c.name LIKE ? OR c.phone LIKE ? OR c.code LIKE ?)");

                String keyword = "%" + filter.getSearch().trim() + "%";
                params.add(keyword);
                params.add(keyword);
                params.add(keyword);
            }

        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Customer c = mapRow(rs);
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Customer findById(int id) {

        String sql = """
            SELECT *
            FROM customer
            WHERE id = ? AND deleted_at IS NULL
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

    @Override
    public boolean insert(Customer request) {

        String sql = """
            INSERT INTO customer
            (code, name, phone, email, address, date_of_birth, gender, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getPhone());
            ps.setString(4, request.getEmail());
            ps.setString(5, request.getAddress());
            ps.setDate(6, request.getDateOfBirth());
            ps.setBoolean(7, request.getGender());
            ps.setInt(8, request.getStatus().getValue());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Customer request) {

        String sql = """
            UPDATE customer
            SET code = ?,
                name = ?,
                phone = ?,
                email = ?,
                address = ?,
                date_of_birth = ?,
                gender = ?,
                status = ?,
                updated_at = ?
            WHERE id = ? AND deleted_at IS NULL
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getPhone());
            ps.setString(4, request.getEmail());
            ps.setString(5, request.getAddress());
            ps.setDate(6, request.getDateOfBirth());
            ps.setBoolean(7, request.getGender());
            ps.setInt(8, request.getStatus().getValue());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(10, request.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int id) {

        String sql = """
            UPDATE customer
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

    private Customer mapRow(ResultSet rs) throws Exception {

        Customer c = new Customer();

        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setDateOfBirth(rs.getDate("date_of_birth"));
        c.setGender(rs.getBoolean("gender"));
        c.setStatus(CustomerStatusEnum.fromValue(rs.getInt("status"))); 

        return c;
    }
    
    public boolean existsByCode(String code) {
        String sql = "SELECT 1 FROM customer WHERE LOWER(code) = LOWER(?) AND deleted_at IS NULL";
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

    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        String sql = "SELECT 1 FROM customer WHERE phone = ? AND deleted_at IS NULL";
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
        String sql = "SELECT 1 FROM customer WHERE LOWER(email) = LOWER(?) AND deleted_at IS NULL";
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
}
