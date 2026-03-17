package dao;

import config.DBConfig;
import entity.Customer;
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
                psInvoice.setInt(2, OrderStatusEnum.PENDING_PAYMENT.getValue());

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
    
    public void updateDiscount(int invoiceId, int discountId, float totalAmount, Integer customerId) {

        StringBuilder sql = new StringBuilder("""
            UPDATE Invoice
            SET discount_id = ?, total_amount = ?
        """);

        Customer customer = null;

        // 👉 nếu có customerId thì lấy thông tin customer
        if (customerId != null) {
            CustomerDAO customerDAO = new CustomerDAO();
            customer = customerDAO.findById(customerId);

            if (customer != null) {
                sql.append("""
                    , customer_id = ?
                    , customer_name = ?
                    , customer_phone = ?
                    , customer_address = ?
                """);
            }
        }

        sql.append(" WHERE id = ?");

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;

            ps.setInt(index++, discountId);
            ps.setFloat(index++, totalAmount);

        if (customer != null) {
            ps.setInt(index++, customer.getId());
            ps.setString(index++, customer.getName());
            ps.setString(index++, customer.getPhone());
            ps.setString(index++, customer.getAddress());
        }

        ps.setInt(index, invoiceId);

        ps.executeUpdate();

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    
    public float getTotalAmount(int invoiceId) {

        String sql = """
            SELECT total_amount
            FROM Invoice
            WHERE id = ?
        """;

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, invoiceId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getFloat("total_amount");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
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
    
    public boolean updateStatus(int invoiceId, OrderStatusEnum status) {

        String sql = """
            UPDATE invoice
            SET status = ?
            WHERE id = ?
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status.getValue());
            ps.setInt(2, invoiceId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public boolean updateTotalAmount(int invoiceId, float totalAmount) {

        String sql = """
            UPDATE invoice
            SET total_amount = ?
            WHERE id = ?
        """;

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setFloat(1, totalAmount);
            ps.setInt(2, invoiceId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}