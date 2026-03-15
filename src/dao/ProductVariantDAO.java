package dao;

import config.DBConfig;
import entity.ProductVariant;
import entity.filter.ProductVariantFilter;
import enums.ProductStatusEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAO implements GenericDAO<ProductVariant, ProductVariantFilter> {

    private DBConfig dbConfig = new DBConfig();
    
    @Override
    public List<ProductVariant> findAll(ProductVariantFilter filter) {

        List<ProductVariant> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                pv.*, 
                p.code AS product_code,
                p.name AS product_name,
                p.description AS description,
                p.status AS status,
                b.name AS brand_name,
                cg.name AS category_name,
                s.name AS size_name,
                c.name AS color_name
            FROM product_variant pv
            LEFT JOIN product p ON pv.product_id = p.id
            LEFT JOIN category cg ON p.category_id = cg.id
            LEFT JOIN brand b ON p.brand_id = b.id
            LEFT JOIN size s ON pv.size_id = s.id
            LEFT JOIN color c ON pv.color_id = c.id
            WHERE pv.deleted_at IS NULL
        """);

        List<Object> params = new ArrayList<>();

        if (filter != null) {

            if (filter.getSizeId() != null) {
                sql.append(" AND pv.size_id = ?");
                params.add(filter.getSizeId());
            }

            if (filter.getColorId() != null) {
                sql.append(" AND pv.color_id = ?");
                params.add(filter.getColorId());
            }

            if (filter.getBrandId() != null) {
                sql.append(" AND p.brand_id = ?");
                params.add(filter.getBrandId());
            }

            if (filter.getCategoryId() != null) {
                sql.append(" AND p.category_id = ?");
                params.add(filter.getCategoryId());
            }

            if (filter.getSearch()!= null && !filter.getSearch().isBlank()) {
                sql.append(" AND (p.name LIKE ? OR p.code LIKE ?)");
                String keyword = "%" + filter.getSearch() + "%";
                params.add(keyword);
                params.add(keyword);
            }
        }
        
        sql.append(" ORDER BY p.code ASC");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductVariant p = mapRow(rs);
                list.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public ProductVariant findById(int id) {
        
        String sql = """
                        SELECT 
                            pv.*, 
                            p.name AS product_name,
                            p.description AS description,
                            p.status AS status,
                            b.name AS brand_name,
                            cg.name AS category_name,
                            s.name AS size_name,
                            c.name AS color_name
                        FROM product_variant pv
                        LEFT JOIN product p ON pv.product_id = p.id
                        LEFT JOIN category cg ON p.category_id = cg.id
                        LEFT JOIN brand b ON p.brand_id = b.id
                        LEFT JOIN size s ON pv.size_id = s.id
                        LEFT JOIN color c ON pv.color_id = c.id
                        WHERE 
                              id = ? AND
                              pv.deleted_at IS NULL
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
    public boolean insert(ProductVariant request) {
        
        String sql = """
                INSERT INTO product_variant
                (product_id, color_id, size_id, price, quantity, image, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, request.getProductId());
            ps.setInt(2, request.getColorId());
            ps.setInt(3, request.getSizeId());
            ps.setFloat(4, request.getPrice());
            ps.setInt(5, request.getQuantity());
            ps.setString(6, request.getImage());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(ProductVariant request) {

        String sql = """
                UPDATE product_variant
                SET product_id = ?,
                    color_id = ?,
                    size_id = ?,
                    price = ?,
                    quantity = ?,
                    image = ?,
                    updated_at = ?
                WHERE id = ?
                AND deleted_at IS NULL
                """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, request.getProductId());
            ps.setInt(2, request.getColorId());
            ps.setInt(3, request.getSizeId());
            ps.setFloat(4, request.getPrice());
            ps.setInt(5, request.getQuantity());
            ps.setString(6, request.getImage());
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
        
        String sql = "UPDATE product_variant SET deleted_at = NOW() WHERE id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    } 
    
    private ProductVariant mapRow(ResultSet rs) throws Exception {

        ProductVariant pv = new ProductVariant();

        pv.setId(rs.getInt("id"));
        pv.setProductId(rs.getInt("product_id"));
        pv.setColorId(rs.getInt("color_id"));
        pv.setSizeId(rs.getInt("size_id"));
        pv.setPrice(rs.getFloat("price"));
        pv.setQuantity(rs.getInt("quantity"));
        pv.setImage(rs.getString("image"));

        pv.setProductCode(rs.getString("product_code"));
        pv.setProductName(rs.getString("product_name"));
        pv.setDescription(rs.getString("description"));

        pv.setBrandName(rs.getString("brand_name"));
        pv.setCategoryName(rs.getString("category_name"));

        pv.setSizeName(rs.getString("size_name"));
        pv.setColorName(rs.getString("color_name"));
        pv.setStatus(ProductStatusEnum.fromValue(rs.getInt("status")));
        
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) pv.setCreatedAt(created.toLocalDateTime());

        if (updated != null) pv.setUpdatedAt(updated.toLocalDateTime());

        return pv;
    }
}
