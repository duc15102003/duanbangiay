package dao;

import config.DBConfig;
import entity.Product;
import entity.filter.ProductFilter;
import enums.ProductStatusEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements GenericDAO<Product, ProductFilter> {
    
    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Product> findAll(ProductFilter filter) {

        List<Product> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                p.*,
                c.name AS category_name,
                b.name AS brand_name
            FROM product p
            LEFT JOIN category c ON p.category_id = c.id
            LEFT JOIN brand b ON p.brand_id = b.id
            WHERE p.deleted_at IS NULL
        """);

        if (filter != null) {

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                sql.append(" AND (p.name LIKE ? OR p.code LIKE ?)");
                String keyword = "%" + filter.getSearch().trim() + "%";
                params.add(keyword);
                params.add(keyword);
            }

            if (filter.getBrandId() != null) {
                sql.append(" AND p.brand_id = ?");
                params.add(filter.getBrandId());
            }

            if (filter.getCategoryId() != null) {
                sql.append(" AND p.category_id = ?");
                params.add(filter.getCategoryId());
            }

            if (filter.getStatus() != null) {
                sql.append(" AND p.status = ?");
                params.add(filter.getStatus().getValue());
            }
        }

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Product p = mapRow(rs);
                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Product findById(int id) {
        
        String sql = """
                        SELECT 
                            p.*, 
                            c.name AS category_name,
                            b.name AS brand_name
                        FROM product p
                        LEFT JOIN category c ON p.category_id = c.id
                        LEFT JOIN brand b ON p.brand_id = b.id
                        WHERE 
                              id = ? AND
                              p.deleted_at IS NULL
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
    public boolean insert(Product request) {

        String sql = """
                INSERT INTO product
                (code, name, description, category_id, brand_id, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getDescription());
            ps.setInt(4, request.getCategoryId());
            ps.setInt(5, request.getBrandId());
            ps.setInt(6, request.getStatus().getValue());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Product request) {

        String sql = """
                UPDATE product
                SET code = ?, 
                    name = ?, 
                    description = ?, 
                    category_id = ?, 
                    brand_id = ?, 
                    status = ?, 
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setString(3, request.getDescription());
            ps.setInt(4, request.getCategoryId());
            ps.setInt(5, request.getBrandId());
            ps.setInt(6, request.getStatus().getValue());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(8, request.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int id) {

        String sql = "UPDATE product SET deleted_at = NOW() WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    private Product mapRow(ResultSet rs) throws Exception {

        Product p = new Product();

        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setBrandId(rs.getInt("brand_id"));
        p.setBrandName(rs.getString("brand_name"));
        p.setStatus(ProductStatusEnum.fromValue(rs.getInt("status"))); 
        
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        if (updated != null) p.setUpdatedAt(updated.toLocalDateTime());

        return p;
    }
}
