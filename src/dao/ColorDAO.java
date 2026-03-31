package dao;

import config.DBConfig;
import entity.Color;
import entity.filter.ColorFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ColorDAO implements GenericDAO<Color, ColorFilter> {
    
    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Color> findAll(ColorFilter filter) {

        List<Color> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT c.*
            FROM color c
            WHERE c.deleted_at IS NULL
        """);

        if (filter != null && filter.getSearch() != null && !filter.getSearch().isBlank()) {

            sql.append("""
                AND (
                    LOWER(c.code) LIKE ?
                    OR LOWER(c.name) LIKE ?
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
    public Color findById(int id) {

        String sql = """
            SELECT *
            FROM color
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
    public boolean insert(Color request) {

        String sql = """
            INSERT INTO color (code, name, created_at)
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
    public boolean update(Color request) {

        String sql = """
            UPDATE color
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
            UPDATE color
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

    private Color mapRow(ResultSet rs) throws Exception {

        Color c = new Color();

        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) c.setCreatedAt(created.toLocalDateTime());
        if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());

        return c;
    }
    
    public boolean existsByCode(String code, Integer excludeId) {
        StringBuilder sql = new StringBuilder("""
            SELECT 1 FROM color
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
            SELECT 1 FROM color
            WHERE LOWER(name) = LOWER(?)
            AND deleted_at IS NULL
        """);
        if (excludeId != null) sql.append(" AND id != ?");

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ) {
            ps.setString(1, name.trim());
            if (excludeId != null) ps.setInt(2, excludeId);
            return ps.executeQuery().next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}