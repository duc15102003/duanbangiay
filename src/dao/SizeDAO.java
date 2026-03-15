package dao;

import config.DBConfig;
import entity.Size;
import entity.filter.SizeFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SizeDAO implements GenericDAO<Size, SizeFilter> {
    
    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Size> findAll(SizeFilter filter) {

        List<Size> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT s.*
            FROM size s
            WHERE s.deleted_at IS NULL
        """);

        if (filter != null && filter.getSearch() != null && !filter.getSearch().isBlank()) {

            sql.append("""
                AND (
                    LOWER(s.code) LIKE ?
                    OR LOWER(s.name) LIKE ?
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
    public Size findById(int id) {

        String sql = """
            SELECT *
            FROM size
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
    public boolean insert(Size request) {

        String sql = """
            INSERT INTO size (code, name, created_at)
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
    public boolean update(Size request) {

        String sql = """
            UPDATE size
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
            UPDATE size
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

    private Size mapRow(ResultSet rs) throws Exception {

        Size s = new Size();

        s.setId(rs.getInt("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));

        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");

        if (created != null) s.setCreatedAt(created.toLocalDateTime());
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());

        return s;
    }
}