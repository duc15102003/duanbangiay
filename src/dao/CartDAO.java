package dao;

import config.DBConfig;
import entity.Invoice;
import entity.InvoiceItem;
import entity.filter.InvoiceFilter;
import enums.OrderStatusEnum;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class CartDAO {

    private DBConfig dbConfig = new DBConfig();
    
    public List<Invoice> findAll(InvoiceFilter filter, Integer employeeId) {

        List<Invoice> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT *
            FROM invoice
            WHERE 1=1
        """);

        if (employeeId != null) {
            sql.append(" AND employee_id = ?");
            params.add(employeeId);
        }

        if (filter != null) {

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                sql.append(" AND (code LIKE ? OR customer_name LIKE ? OR customer_phone LIKE ?)");

                String keyword = "%" + filter.getSearch().trim() + "%";

                params.add(keyword); // code
                params.add(keyword); // customer_name
                params.add(keyword); // customer_phone
            }

            if (filter.getStatus() != null) {
                sql.append(" AND status = ?");
                params.add(filter.getStatus().getValue());
            }
            
            if (filter.getPaymentType()!= null) {
                sql.append(" AND payment_type = ?");
                params.add(filter.getPaymentType());
            }

            if (filter.getFromCreatedDate() != null) {
                sql.append(" AND created_at >= ?");
                params.add(Timestamp.valueOf(filter.getFromCreatedDate()));
            }

            if (filter.getToCreatedDate() != null) {
                sql.append(" AND created_at <= ?");
                params.add(Timestamp.valueOf(filter.getToCreatedDate()));
            }
        }

        sql.append(" ORDER BY created_at DESC");

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRowInvoice(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống!");
        }

        return list;
    }
    
    public List<InvoiceItem> findByInvoiceId(int invoiceId, String keyword) {

        List<InvoiceItem> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                ii.*,
                i.code AS invoice_code,

                p.id AS product_id,
                p.code AS product_code,
                p.name AS product_name,

                b.id AS brand_id,
                b.name AS brand_name,

                c.id AS category_id,
                c.name AS category_name,

                s.id AS size_id,
                s.name AS size_name,

                co.id AS color_id,
                co.name AS color_name,

                pv.image,
                pv.price

            FROM invoice_item ii
            LEFT JOIN invoice i ON ii.invoice_id = i.id
            LEFT JOIN product_variant pv ON ii.product_variant_id = pv.id
            LEFT JOIN product p ON pv.product_id = p.id
            LEFT JOIN brand b ON p.brand_id = b.id
            LEFT JOIN category c ON p.category_id = c.id
            LEFT JOIN size s ON pv.size_id = s.id
            LEFT JOIN color co ON pv.color_id = co.id
            WHERE ii.invoice_id = ?
        """);

        if(keyword != null && !keyword.isBlank()){
            sql.append("""
                AND (
                    p.name LIKE ?
                    OR b.name LIKE ?
                    OR c.name LIKE ?
                )
            """);
        }

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());
        ) {

            ps.setInt(1, invoiceId);

            if(keyword != null && !keyword.isBlank()){
                String k = "%" + keyword.trim() + "%";
                ps.setString(2, k);
                ps.setString(3, k);
                ps.setString(4, k);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapRowInvoiceItem(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    
    public boolean updateQuantity(int invoiceId, int productVariantId, int quantity) {

        String sql = """
            UPDATE invoice_item
            SET quantity = ?
            WHERE invoice_id = ? AND product_variant_id = ?
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setInt(1, quantity);
            ps.setInt(2, invoiceId);
            ps.setInt(3, productVariantId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public boolean deleteRowCart(int invoiceId, int productVariantId) {

        String sql = """
            DELETE FROM invoice_item
            WHERE invoice_id = ? AND product_variant_id = ?
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setInt(1, invoiceId);
            ps.setInt(2, productVariantId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean addProductToCart(int invoiceId, int productVariantId, int quantity, float price) {

        String stockSQL = """
            SELECT quantity
            FROM product_variant
            WHERE id = ?
        """;

        String checkSQL = """
            SELECT quantity
            FROM invoice_item
            WHERE invoice_id = ? AND product_variant_id = ?
        """;

        String insertSQL = """
            INSERT INTO invoice_item(invoice_id, product_variant_id, quantity, price)
            VALUES (?,?,?,?)
        """;

        String updateSQL = """
            UPDATE invoice_item
            SET quantity = quantity + ?
            WHERE invoice_id = ? AND product_variant_id = ?
        """;

        try (Connection conn = dbConfig.getConnection()) {

            int stock = 0;

            // Lấy tồn kho
            try (PreparedStatement ps = conn.prepareStatement(stockSQL)) {
                ps.setInt(1, productVariantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stock = rs.getInt("quantity");
                    }
                }
            }

            int currentQuantity = 0;
            boolean exists = false;

            // Kiểm tra sản phẩm đã có trong giỏ
            try (PreparedStatement ps = conn.prepareStatement(checkSQL)) {
                ps.setInt(1, invoiceId);
                ps.setInt(2, productVariantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentQuantity = rs.getInt("quantity");
                        exists = true;
                    }
                }
            }

            // Check tồn kho
//            if (stock > (quantity + currentQuantity)) {
//                 JOptionPane.showMessageDialog(
//                     null,
//                     "Số lượng vượt quá tồn kho!\nChỉ còn " + (stock) + " sản phẩm.",
//                     "Thông báo",
//                     JOptionPane.WARNING_MESSAGE
//                 );
//                 return false;
//             }

            if (exists) {

                try (PreparedStatement ps = conn.prepareStatement(updateSQL)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, invoiceId);
                    ps.setInt(3, productVariantId);

                    return ps.executeUpdate() > 0;
                }

            } else {

                try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                    ps.setInt(1, invoiceId);
                    ps.setInt(2, productVariantId);
                    ps.setInt(3, quantity);
                    ps.setFloat(4, price);

                    return ps.executeUpdate() > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi hệ thống!");
        }

        return false;
    }
    
    private InvoiceItem mapRowInvoiceItem(ResultSet rs) throws Exception {

        InvoiceItem i = new InvoiceItem();

        i.setId(rs.getInt("id"));
        i.setInvoiceId(rs.getInt("invoice_id"));
        i.setProductVariantId(rs.getInt("product_variant_id"));

        i.setQuantity(rs.getInt("quantity"));
        i.setPrice(rs.getFloat("price"));

        i.setInvoiceCode(rs.getString("invoice_code"));

        i.setBrandId(rs.getInt("brand_id"));
        i.setBrandName(rs.getString("brand_name"));

        i.setCategoryId(rs.getInt("category_id"));
        i.setCategoryName(rs.getString("category_name"));

        i.setSizeId(rs.getInt("size_id"));
        i.setSizeName(rs.getString("size_name"));

        i.setColorId(rs.getInt("color_id"));
        i.setColorName(rs.getString("color_name"));

        i.setProductId(rs.getInt("product_id"));
        i.setProductCode(rs.getString("product_code"));
        i.setProductName(rs.getString("product_name"));

        i.setImage(rs.getString("image"));

        return i;
    }
    
    private Invoice mapRowInvoice(ResultSet rs) throws Exception {

        Invoice i = new Invoice();

        i.setId(rs.getInt("id"));
        i.setCode(rs.getString("code"));
        i.setEmployeeId(rs.getInt("employee_id"));
        i.setCustomerId(rs.getInt("customer_id"));

        i.setCustomerName(rs.getString("customer_name"));
        i.setCustomerPhone(rs.getString("customer_phone"));
        i.setCustomerAddress(rs.getString("customer_address"));

        i.setEmployeeName(rs.getString("employee_name"));
        i.setDiscountType(rs.getString("discount_type"));
        i.setDiscountAmount(rs.getInt("discount_amount"));

        i.setTotalAmount(rs.getFloat("total_amount"));
        i.setPaymentType(rs.getString("payment_type"));
        i.setStatus(OrderStatusEnum.fromValue(rs.getInt("status")));

        Timestamp created = rs.getTimestamp("created_at");

        if (created != null) {
            i.setCreatedAt(created.toLocalDateTime());
        }
        
        return i;
    }
    
    public InvoiceItem findItem(int invoiceId, int productVariantId) {

        String sql = """
            SELECT *
            FROM invoice_item
            WHERE invoice_id = ? AND product_variant_id = ?
        """;

        try (
            Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {

            ps.setInt(1, invoiceId);
            ps.setInt(2, productVariantId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                InvoiceItem item = new InvoiceItem();
                item.setInvoiceId(rs.getInt("invoice_id"));
                item.setProductVariantId(rs.getInt("product_variant_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getFloat("price"));
                return item;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}