package dao;

import config.DBConfig;
import entity.RevenueDTO;
import enums.OrderStatusEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public List<RevenueDTO> getRevenue(String type, LocalDateTime from, LocalDateTime to) {

        List<RevenueDTO> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();

        switch (type) {
            case "DAY":
                sql.append("""
                    SELECT 
                        CONVERT(VARCHAR, created_at, 103) AS label, -- dd/MM/yyyy
                        SUM(total_amount) AS total,
                        MIN(created_at) as sort_date
                    FROM invoice
                    WHERE status = ?
                """);
                break;

            case "MONTH":
                sql.append("""
                    SELECT 
                        YEAR(created_at) as y,
                        MONTH(created_at) as m,
                        SUM(total_amount) AS total
                    FROM invoice
                    WHERE status = ?
                """);
                break;

            case "YEAR":
                sql.append("""
                    SELECT 
                        YEAR(created_at) as y,
                        SUM(total_amount) AS total
                    FROM invoice
                    WHERE status = ?
                """);
                break;
        }

        // ===== FILTER =====
        if (from != null) {
            sql.append(" AND created_at >= ?");
        }

        if (to != null) {
            sql.append(" AND created_at <= ?");
        }

        // ===== GROUP =====
        switch (type) {
            case "DAY":
                sql.append(" GROUP BY CONVERT(VARCHAR, created_at, 103) ORDER BY sort_date");
                break;

            case "MONTH":
                sql.append(" GROUP BY YEAR(created_at), MONTH(created_at) ORDER BY y, m");
                break;

            case "YEAR":
                sql.append(" GROUP BY YEAR(created_at) ORDER BY y");
                break;
        }

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            ps.setInt(index++, OrderStatusEnum.PAID.getValue());

            if (from != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(from));
            }

            if (to != null) {
                ps.setTimestamp(index++, Timestamp.valueOf(to));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                switch (type) {
                    case "DAY":
                        list.add(new RevenueDTO(
                                rs.getString("label"),
                                rs.getFloat("total")
                        ));
                        break;

                    case "MONTH":
                        list.add(new RevenueDTO(
                                "T" + rs.getInt("m") + "/" + rs.getInt("y"),
                                rs.getFloat("total")
                        ));
                        break;

                    case "YEAR":
                        list.add(new RevenueDTO(
                                String.valueOf(rs.getInt("y")),
                                rs.getFloat("total")
                        ));
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public List<RevenueDTO> getRevenueByPaymentType(LocalDateTime from, LocalDateTime to) {
        List<RevenueDTO> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                i.payment_type AS label,
                SUM(i.total_amount) AS total,
                COUNT(i.id) AS total_count
            FROM invoice i
            WHERE i.status = ?
        """);

        List<Object> params = new ArrayList<>();
        params.add(OrderStatusEnum.PAID.getValue());

        if (from != null) {
            sql.append(" AND i.created_at >= ?");
            params.add(Timestamp.valueOf(from));
        }
        if (to != null) {
            sql.append(" AND i.created_at <= ?");
            params.add(Timestamp.valueOf(to));
        }

        sql.append(" GROUP BY i.payment_type");

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                RevenueDTO dto = new RevenueDTO(
                    rs.getString("label"),
                    rs.getFloat("total"),
                    rs.getInt("total_count")
                );
                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}