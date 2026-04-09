package dao;

import config.DBConfig;
import entity.Brand;
import entity.filter.BrandFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO implements GenericDAO<Brand, BrandFilter> {
    
    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Brand> findAll(BrandFilter filter) {

        List<Brand> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                b.*
            FROM brand b
            WHERE b.deleted_at IS NULL
        """);

        if (filter != null && filter.getSearch() != null && !filter.getSearch().isBlank()) {

            sql.append("""
                AND (
                    LOWER(b.code) LIKE ?
                    OR LOWER(b.name) LIKE ?
                )
            """);

            String keyword = "%" + filter.getSearch().trim().toLowerCase() + "%";

            params.add(keyword);
            params.add(keyword);
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
                list.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Brand findById(int id) {
        String sql = """
            SELECT *
            FROM brand
            WHERE id = ?
            AND deleted_at IS NULL
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

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
    public boolean insert(Brand request) {
        String sql = """
            INSERT INTO brand (code, name, created_at)
            VALUES (?, ?, GETDATE())
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Brand request) {
        String sql = """
            UPDATE brand
            SET code = ?, name = ?, updated_at = GETDATE()
            WHERE id = ?
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getName());
            ps.setInt(3, request.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = """
            UPDATE brand
            SET deleted_at = GETDATE()
            WHERE id = ?
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    private Brand mapRow(ResultSet rs) throws Exception {

        Brand b = new Brand();

        b.setId(rs.getInt("id"));
        b.setCode(rs.getString("code"));
        b.setName(rs.getString("name")); 

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) b.setCreatedAt(created.toLocalDateTime());
        if (updated != null) b.setUpdatedAt(updated.toLocalDateTime());

        return b;
    }
    
    public boolean existsByCode(String code, Integer excludeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT 1 FROM brand
            WHERE LOWER(code) = LOWER(?)
            AND deleted_at IS NULL
        """);
        if (excludeId != null) sql.append(" AND id != ?");

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ) {
            ps.setString(1, code.trim());
            if (excludeId != null) ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByName(String name, Integer excludeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT 1 FROM brand
            WHERE LOWER(name) = LOWER(?)
            AND deleted_at IS NULL
        """);

        if (excludeId != null) {
            sql.append(" AND id != ?");
        }

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ) {
            ps.setString(1, name.trim());
            if (excludeId != null) {
                ps.setInt(2, excludeId);
            }

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
