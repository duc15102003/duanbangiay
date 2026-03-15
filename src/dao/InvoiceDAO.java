package dao;

import config.DBConfig;
import entity.Invoice;
import enums.OrderStatusEnum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class InvoiceDAO {
    
    private DBConfig dbConfig = new DBConfig();
        
    public boolean insert(Invoice invoice) {

        String sql = """
            INSERT INTO invoice (
                code,
                employee_id,
                total_amount,
                status
            )
            VALUES (?, ?, ?, ?)
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            String code = generateInvoiceCode(conn);
            invoice.setCode(code);

            ps.setString(1, code);
            ps.setInt(2, invoice.getEmployeeId());
            //ps.setInt(3, invoice.getCustomerId());
            ps.setFloat(3, invoice.getTotalAmount());
            ps.setInt(4, invoice.getStatus().getValue());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public boolean delete(int invoiceId) {

        String deleteInvoiceItem = "DELETE FROM invoice_item WHERE invoice_id = ?";
        String deleteInvoice = """
            DELETE FROM invoice
            WHERE id = ?
            AND status = ?
        """;

        try (
            Connection conn = dbConfig.getConnection();
        ) {

            conn.setAutoCommit(false);

            try (
                PreparedStatement psDetail = conn.prepareStatement(deleteInvoiceItem);
                PreparedStatement psInvoice = conn.prepareStatement(deleteInvoice);
            ) {

                psDetail.setInt(1, invoiceId);
                psDetail.executeUpdate();

                psInvoice.setInt(1, invoiceId);
                psInvoice.setInt(2, OrderStatusEnum.DRAFT.getValue());

                int result = psInvoice.executeUpdate();

                conn.commit();

                return result > 0;

            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
        
    private String generateInvoiceCode(Connection conn) throws Exception {

        String sql = """
            SELECT TOP 1 code
            FROM invoice
            WHERE code LIKE N'HĐ%'
            ORDER BY code DESC
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {

                String lastCode = rs.getString("code");
                String numberPart = lastCode.substring(2);
                int number = Integer.parseInt(numberPart) + 1;

                return "HĐ" + String.format("%05d", number);
            }
        }

        return "HĐ00001";
    }
}