    package dao;

    import config.DBConfig;
    import entity.Customer;
    import entity.Invoice;
    import enums.OrderStatusEnum;

    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.ResultSet;
    import java.sql.Types;
    
    import java.sql.Timestamp;

    public class InvoiceDAO {

        private DBConfig dbConfig = new DBConfig();

        public Invoice findById(int id) {

            String sql = """
                SELECT *
                FROM Invoice
                WHERE id = ?
            """;

            try (Connection conn = DBConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    Invoice invoice = new Invoice();

                    invoice.setId(rs.getInt("id"));
                    invoice.setCode(rs.getString("code"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    invoice.setDiscountAmount(rs.getInt("discount_amount"));
                    invoice.setTotalAmount(rs.getInt("total_amount"));
                    if (ts != null) {
                        invoice.setCreatedAt(ts.toLocalDateTime());
                    }
                    invoice.setEmployeeName(rs.getString("employee_name"));
                    invoice.setEmployeeId(rs.getInt("employee_id"));
                    invoice.setStatus(OrderStatusEnum.fromValue(rs.getInt("status")));

                    return invoice;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public boolean insert(Invoice invoice) {

            String sql = """
                INSERT INTO invoice (
                    code,
                    employee_id,
                    total_amount,
                    status,
                    created_at
                )
                VALUES (?, ?, ?, ?, GETDATE())
            """;

            try (
                Connection conn = DBConfig.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
            ) {

                String code = generateInvoiceCode(conn);
                invoice.setCode(code);

                ps.setString(1, code);
                ps.setInt(2, invoice.getEmployeeId());
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
        
        public boolean updateInvoicePaymentInfo(Invoice invoice) {
            String sql = """
                UPDATE invoice
                SET employee_id = ?, 
                    customer_id = ?, 
                    customer_name = ?, 
                    customer_phone = ?, 
                    customer_address = ?, 
                    employee_name = ?,
                    discount_amount = ?,
                    discount_id = ?,
                    discount_type = ?
                WHERE id = ?
            """;
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setObject(1, invoice.getEmployeeId());
                ps.setObject(2, invoice.getCustomerId());
                ps.setString(3, invoice.getCustomerName());
                ps.setString(4, invoice.getCustomerPhone());
                ps.setString(5, invoice.getCustomerAddress());
                ps.setString(6, invoice.getEmployeeName());
                ps.setLong(7, invoice.getDiscountAmount());
                if (invoice.getDiscountId() != null) {
                    ps.setInt(8, invoice.getDiscountId());
                } else {
                    ps.setNull(8, Types.INTEGER);
                }
                ps.setString(9, invoice.getDiscountType());
                ps.setInt(10, invoice.getId());
                return ps.executeUpdate() > 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        
        public boolean updatePaymentType(int invoiceId, String paymentType) {

            String sql = """
                UPDATE invoice
                SET payment_type = ?
                WHERE id = ?
            """;

            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, paymentType);
                ps.setInt(2, invoiceId);

                return ps.executeUpdate() > 0;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }