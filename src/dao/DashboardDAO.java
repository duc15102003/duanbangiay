package dao;

import config.DBConfig;
import entity.DashboardSummaryDTO;
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
                        CONVERT(VARCHAR, i.created_at, 103) AS label,
                        SUM(i.total_amount) AS total,
                        SUM(ii.quantity) AS total_count,
                        MIN(i.created_at) as sort_date
                    FROM invoice i
                    JOIN invoice_item ii ON ii.invoice_id = i.id
                    WHERE i.status = ?
                """);
                break;

            case "MONTH":
                sql.append("""
                    SELECT 
                        YEAR(i.created_at) as y,
                        MONTH(i.created_at) as m,
                        SUM(i.total_amount) AS total,
                        SUM(ii.quantity) AS total_count
                    FROM invoice i
                    JOIN invoice_item ii ON ii.invoice_id = i.id
                    WHERE i.status = ?
                """);
                break;

            case "YEAR":
                sql.append("""
                    SELECT 
                        YEAR(i.created_at) as y,
                        SUM(i.total_amount) AS total,
                        SUM(ii.quantity) AS total_count
                    FROM invoice i
                    JOIN invoice_item ii ON ii.invoice_id = i.id
                    WHERE i.status = ?
                """);
                break;
        }

        if (from != null) {
            sql.append(" AND i.created_at >= ?");
        }
        if (to != null) {
            sql.append(" AND i.created_at <= ?");
        }

        switch (type) {
            case "DAY":
                sql.append(" GROUP BY CONVERT(VARCHAR, i.created_at, 103) ORDER BY sort_date");
                break;
            case "MONTH":
                sql.append(" GROUP BY YEAR(i.created_at), MONTH(i.created_at) ORDER BY y, m");
                break;
            case "YEAR":
                sql.append(" GROUP BY YEAR(i.created_at) ORDER BY y");
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
                            rs.getFloat("total"),
                            rs.getInt("total_count")
                        ));
                        break;

                    case "MONTH":
                        list.add(new RevenueDTO(
                            "T" + rs.getInt("m") + "/" + rs.getInt("y"),
                            rs.getFloat("total"),
                            rs.getInt("total_count")
                        ));
                        break;

                    case "YEAR":
                        list.add(new RevenueDTO(
                            String.valueOf(rs.getInt("y")),
                            rs.getFloat("total"),
                            rs.getInt("total_count")
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
    
    public DashboardSummaryDTO getDashboardSummary() {

        String sql = """
            SELECT 
                SUM(CASE 
                    WHEN CAST(created_at AS DATE) = CAST(GETDATE() AS DATE) 
                    THEN 1 ELSE 0 END) AS today_invoice_count,

                SUM(CASE 
                    WHEN CAST(created_at AS DATE) = CAST(GETDATE() AS DATE) 
                    THEN total_amount ELSE 0 END) AS today_revenue,

                SUM(CASE 
                    WHEN CAST(created_at AS DATE) = CAST(DATEADD(DAY, -1, GETDATE()) AS DATE) 
                    THEN 1 ELSE 0 END) AS yesterday_invoice_count

            FROM invoice
            WHERE status = ?
        """;

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, OrderStatusEnum.PAID.getValue());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new DashboardSummaryDTO(
                    rs.getInt("today_invoice_count"),
                    rs.getDouble("today_revenue"),
                    rs.getInt("yesterday_invoice_count")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DashboardSummaryDTO(0, 0, 0);
    }
}