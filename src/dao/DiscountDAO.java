package dao;

import config.DBConfig;
import entity.Discount;
import entity.filter.DiscountFilter;
import enums.DiscountStatusEnum;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class DiscountDAO implements GenericDAO<Discount, DiscountFilter> {

    private DBConfig dbConfig = new DBConfig();

    @Override
    public List<Discount> findAll(DiscountFilter filter) {
        List<Discount> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT d.*
            FROM discount d
            WHERE d.deleted_at IS NULL
        """);

        if (filter != null) {
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                sql.append(" AND LOWER(d.code) LIKE ?");
                params.add("%" + filter.getSearch().trim().toLowerCase() + "%");
            }
            if (filter.getStatus() != null) {
                sql.append(" AND d.status = ?");
                params.add(filter.getStatus().getValue());
            }
            if (filter.getFromStartedAt() != null) {
                sql.append(" AND d.started_at >= ?");
                params.add(Timestamp.valueOf(filter.getFromStartedAt()));
            }
            if (filter.getToEndedAt() != null) {
                sql.append(" AND d.ended_at <= ?");
                params.add(Timestamp.valueOf(filter.getToEndedAt()));
            }
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

    @Override
    public Discount findById(int id) {
        String sql = """
            SELECT *
            FROM discount
            WHERE id = ? AND deleted_at IS NULL
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean insert(Discount request) {
        String sql = """
            INSERT INTO discount
            (code, discount_type, discount_value, maximum_discount,
             started_at, ended_at, status, discount_condition, quantity, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getDiscountType());
            ps.setInt(3, request.getDiscountValue());

            if (request.getMaximumDiscount() != null) {
                ps.setInt(4, request.getMaximumDiscount());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (request.getStartedAt() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(request.getStartedAt()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            if (request.getEndedAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(request.getEndedAt()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.setInt(7, request.getStatus().getValue());
            ps.setString(8, request.getDiscountCondition());

            if (request.getQuantity() != null) {
                ps.setInt(9, request.getQuantity());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean update(Discount request) {
        String sql = """
            UPDATE discount
            SET code = ?, discount_type = ?, discount_value = ?, maximum_discount = ?,
                started_at = ?, ended_at = ?, status = ?, discount_condition = ?, quantity = ?, updated_at = GETDATE()
            WHERE id = ?
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, request.getCode());
            ps.setString(2, request.getDiscountType());
            ps.setInt(3, request.getDiscountValue());

            if (request.getMaximumDiscount() != null) {
                ps.setInt(4, request.getMaximumDiscount());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (request.getStartedAt() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(request.getStartedAt()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            if (request.getEndedAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(request.getEndedAt()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }

            ps.setInt(7, request.getStatus().getValue());
            ps.setString(8, request.getDiscountCondition());

            if (request.getQuantity() != null) {
                ps.setInt(9, request.getQuantity());
            } else {
                ps.setNull(9, Types.INTEGER);
            }

            ps.setInt(10, request.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "UPDATE discount SET deleted_at = GETDATE() WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Discount mapRow(ResultSet rs) throws Exception {
        Discount d = new Discount();

        d.setId(rs.getInt("id"));
        d.setCode(rs.getString("code"));
        d.setDiscountType(rs.getString("discount_type"));
        d.setDiscountValue(rs.getInt("discount_value"));
        d.setMaximumDiscount(rs.getInt("maximum_discount"));
        d.setStartedAt(rs.getTimestamp("started_at") != null ? rs.getTimestamp("started_at").toLocalDateTime() : null);
        d.setEndedAt(rs.getTimestamp("ended_at") != null ? rs.getTimestamp("ended_at").toLocalDateTime() : null);
        d.setStatus(DiscountStatusEnum.fromValue(rs.getInt("status")));
        d.setDiscountCondition(rs.getString("discount_condition"));

        int qty = rs.getInt("quantity");
        d.setQuantity(rs.wasNull() ? null : qty);

        d.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
        d.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);

        return d;
    }

    public boolean existsByCode(String code, Integer excludeId) {
        if (code == null || code.trim().isEmpty()) return false;

        String sql = "SELECT 1 FROM discount WHERE LOWER(code) = LOWER(?) AND deleted_at IS NULL";
        if (excludeId != null) sql += " AND id <> ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            if (excludeId != null) ps.setInt(2, excludeId);

            return ps.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public Discount checkDiscount(String code) {
        if (code == null || code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập mã giảm giá!");
            return null;
        }

        String sql = "SELECT * FROM Discount WHERE LOWER(code) = LOWER(?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá không tồn tại!");
                return null;
            }

            Discount discount = mapRow(rs);

            if (discount.getStatus() != DiscountStatusEnum.ACTIVE) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá không ở trạng thái hoạt động!");
                return null;
            }

            LocalDateTime now = LocalDateTime.now();

            if (discount.getStartedAt() != null && discount.getStartedAt().isAfter(now)) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá chưa đến thời gian sử dụng!");
                return null;
            }
            if (discount.getEndedAt() != null && discount.getEndedAt().isBefore(now)) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá đã hết hạn!");
                return null;
            }

            if (discount.getQuantity() <= 0) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá đã hết số lượng sử dụng!");
                return null;
            }

            return discount;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Có lỗi xảy ra khi kiểm tra mã giảm giá!");
        }

        return null;
    }
    
    public boolean useDiscount(String code) {
        if (code == null || code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập mã giảm giá!");
            return false;
        }

        String sql = """
            UPDATE Discount
            SET quantity = quantity - 1
            OUTPUT inserted.*
            WHERE LOWER(code) = LOWER(?)
              AND quantity > 0
        """;

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá không tồn tại hoặc đã hết số lượng!");
                return false;
            }

            Discount discount = mapRow(rs);

            LocalDateTime now = LocalDateTime.now();
            if (discount.getStatus() != DiscountStatusEnum.ACTIVE) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá không ở trạng thái hoạt động!");
                return false;
            }
            if (discount.getStartedAt() != null && discount.getStartedAt().isAfter(now)) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá chưa đến thời gian sử dụng!");
                return false;
            }
            if (discount.getEndedAt() != null && discount.getEndedAt().isBefore(now)) {
                JOptionPane.showMessageDialog(null, "Mã giảm giá đã hết hạn!");
                return false;
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Có lỗi xảy ra khi sử dụng mã giảm giá!");
            return false;
        }
    }
}