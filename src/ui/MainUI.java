    package ui;

import java.awt.CardLayout;
import javax.swing.JOptionPane;
import service.BrandService;
import service.CategoryService;
import service.ColorService;
import service.DiscountService;
import service.SizeService;

public class MainUI extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainUI.class.getName());
    
    private DashboardUI dashboardUI;
    private ProductManagerUI productManagerUI;
    private CustomerUI customerUI;
    private EmployeeUI employeeUI;
    private InvoiceUI invoiceUI;
    private DiscountUI discountUI;
    private AttributeUI attributeUI;
    private OrderUI orderUI;
    
    private BrandService brandService = new BrandService();
    private CategoryService categoryService = new CategoryService();
    private SizeService sizeService = new SizeService();
    private ColorService colorService = new ColorService();
    private DiscountService discountService = new DiscountService();
    
    private int customerId;

    public MainUI() {
        this(0, null, null);
    }

    public MainUI(int customerId, String name, String screen) {
        initComponents();
        setLocationRelativeTo(null);

        this.customerId = customerId;
        
        if (name != null) {
            lbName.setText(name);
        }

        pnContent.revalidate();
        pnContent.repaint();

        initView();
        
        if ("product".equals(screen)) {
            showProduct();
        } else {
            showDashboard();
        }
    }

    private void initView() {

        dashboardUI = new DashboardUI();
        productManagerUI = new ProductManagerUI();
        customerUI = new CustomerUI(customerId);
        employeeUI = new EmployeeUI(customerId);
        invoiceUI = new InvoiceUI();
        discountUI = new DiscountUI();
        attributeUI = new AttributeUI();
        orderUI = new OrderUI(customerId);

        pnContent.add(dashboardUI, "dashboard");
        pnContent.add(productManagerUI, "product_manager");
        pnContent.add(customerUI, "customer");
        pnContent.add(employeeUI, "employee");
        pnContent.add(invoiceUI, "invoice");
        pnContent.add(discountUI, "discount");
        pnContent.add(attributeUI, "attribute");
        
        showDashboard();
    }

    private void showDashboard() {
        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "dashboard");
        dashboardUI.loadDashboardSummary();
    }

    private void showProduct() {
        //productManagerUI.reloadData();
        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "product_manager");
    }
    
    private void showAttribute() {
        //productManagerUI.reloadData();
        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "attribute");
    }
    
    private void showCustomer(){
        customerUI.resetForm();
        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "customer");
    }
    
    private void showEmployee(){
        employeeUI.resetForm();
        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "employee");
    }
    
    private void showInvoice(){

        invoiceUI.loadFilterData(
            brandService.findAll(null),
            categoryService.findAll(null),
            sizeService.findAll(null),
            colorService.findAll(null)
        );

        invoiceUI.resetForm();

        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "invoice");
    }
    
    private void showDiscount(){
        discountUI.refreshForm();
        CardLayout card = (CardLayout) pnContent.getLayout();
        card.show(pnContent, "discount");
    }
    
    private void showOrder(){
        new OrderUI(customerId).setVisible(true);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnMenu = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lbDashboard = new javax.swing.JLabel();
        ldProduct = new javax.swing.JLabel();
        lbEmployee = new javax.swing.JLabel();
        lbCustomer = new javax.swing.JLabel();
        lbInvoice = new javax.swing.JLabel();
        lbName = new javax.swing.JLabel();
        lbLogout = new javax.swing.JLabel();
        lbInvoice1 = new javax.swing.JLabel();
        lbDashboard1 = new javax.swing.JLabel();
        lbDashboard2 = new javax.swing.JLabel();
        pnContent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnMenu.setBackground(new java.awt.Color(4, 4, 31));
        pnMenu.setForeground(new java.awt.Color(102, 102, 102));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("SimSun", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Xin chào");

        lbDashboard.setBackground(new java.awt.Color(255, 255, 255));
        lbDashboard.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbDashboard.setForeground(new java.awt.Color(255, 255, 255));
        lbDashboard.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbDashboard.setText("Dashboard");
        lbDashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbDashboardMouseClicked(evt);
            }
        });

        ldProduct.setBackground(new java.awt.Color(255, 255, 255));
        ldProduct.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        ldProduct.setForeground(new java.awt.Color(255, 255, 255));
        ldProduct.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ldProduct.setText("Sản phẩm");
        ldProduct.setToolTipText("");
        ldProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ldProductMouseClicked(evt);
            }
        });

        lbEmployee.setBackground(new java.awt.Color(255, 255, 255));
        lbEmployee.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbEmployee.setForeground(new java.awt.Color(255, 255, 255));
        lbEmployee.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbEmployee.setText("Nhân  viên");
        lbEmployee.setToolTipText("");
        lbEmployee.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbEmployeeMouseClicked(evt);
            }
        });

        lbCustomer.setBackground(new java.awt.Color(255, 255, 255));
        lbCustomer.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbCustomer.setForeground(new java.awt.Color(255, 255, 255));
        lbCustomer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbCustomer.setText("Khách hàng");
        lbCustomer.setToolTipText("");
        lbCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbCustomerMouseClicked(evt);
            }
        });

        lbInvoice.setBackground(new java.awt.Color(255, 255, 255));
        lbInvoice.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbInvoice.setForeground(new java.awt.Color(255, 255, 255));
        lbInvoice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbInvoice.setText("Hoá đơn");
        lbInvoice.setToolTipText("");
        lbInvoice.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbInvoiceMouseClicked(evt);
            }
        });

        lbName.setBackground(new java.awt.Color(255, 255, 255));
        lbName.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lbName.setForeground(new java.awt.Color(255, 255, 255));
        lbName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbName.setText("Name");

        lbLogout.setBackground(new java.awt.Color(255, 102, 102));
        lbLogout.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbLogout.setForeground(new java.awt.Color(255, 102, 102));
        lbLogout.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbLogout.setText("Đăng xuất");
        lbLogout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbLogoutMouseClicked(evt);
            }
        });

        lbInvoice1.setBackground(new java.awt.Color(255, 255, 255));
        lbInvoice1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbInvoice1.setForeground(new java.awt.Color(255, 255, 255));
        lbInvoice1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbInvoice1.setText("Giảm giá");
        lbInvoice1.setToolTipText("");
        lbInvoice1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbInvoice1MouseClicked(evt);
            }
        });

        lbDashboard1.setBackground(new java.awt.Color(255, 255, 255));
        lbDashboard1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbDashboard1.setForeground(new java.awt.Color(255, 255, 255));
        lbDashboard1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbDashboard1.setText("Thuộc tính");
        lbDashboard1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbDashboard1MouseClicked(evt);
            }
        });

        lbDashboard2.setBackground(new java.awt.Color(255, 255, 255));
        lbDashboard2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lbDashboard2.setForeground(new java.awt.Color(255, 255, 255));
        lbDashboard2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbDashboard2.setText("Bán hàng");
        lbDashboard2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbDashboard2MouseClicked(evt);
            }
        });

        pnContent.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout pnMenuLayout = new javax.swing.GroupLayout(pnMenu);
        pnMenu.setLayout(pnMenuLayout);
        pnMenuLayout.setHorizontalGroup(
            pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMenuLayout.createSequentialGroup()
                .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnMenuLayout.createSequentialGroup()
                        .addGap(100, 100, 100)
                        .addGroup(pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ldProduct, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbDashboard, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbEmployee, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbCustomer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbInvoice, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbInvoice1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbDashboard1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lbDashboard2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(106, 106, 106))
                    .addComponent(lbLogout, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnContent, javax.swing.GroupLayout.PREFERRED_SIZE, 994, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        pnMenuLayout.setVerticalGroup(
            pnMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnMenuLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(lbName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lbDashboard2)
                .addGap(40, 40, 40)
                .addComponent(lbDashboard)
                .addGap(40, 40, 40)
                .addComponent(ldProduct)
                .addGap(40, 40, 40)
                .addComponent(lbDashboard1)
                .addGap(39, 39, 39)
                .addComponent(lbEmployee)
                .addGap(40, 40, 40)
                .addComponent(lbCustomer)
                .addGap(40, 40, 40)
                .addComponent(lbInvoice1)
                .addGap(40, 40, 40)
                .addComponent(lbInvoice)
                .addGap(42, 42, 42)
                .addComponent(lbLogout)
                .addGap(23, 23, 23))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnMenuLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(pnContent, javax.swing.GroupLayout.PREFERRED_SIZE, 703, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnMenu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ldProductMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ldProductMouseClicked
        showProduct();
    }//GEN-LAST:event_ldProductMouseClicked

    private void lbEmployeeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbEmployeeMouseClicked
        showEmployee();
    }//GEN-LAST:event_lbEmployeeMouseClicked

    private void lbCustomerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbCustomerMouseClicked
        showCustomer();
    }//GEN-LAST:event_lbCustomerMouseClicked

    private void lbInvoiceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbInvoiceMouseClicked
        showInvoice();
    }//GEN-LAST:event_lbInvoiceMouseClicked

    private void lbLogoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbLogoutMouseClicked
        int confirm = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Bạn có muốn đăng xuất không?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if(confirm == javax.swing.JOptionPane.YES_OPTION){
            new ui.auth.LoginUI().setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_lbLogoutMouseClicked

    private void lbInvoice1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbInvoice1MouseClicked
        showDiscount();
    }//GEN-LAST:event_lbInvoice1MouseClicked

    private void lbDashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDashboardMouseClicked
        showDashboard();
    }//GEN-LAST:event_lbDashboardMouseClicked

    private void lbDashboard1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDashboard1MouseClicked
        showAttribute();
    }//GEN-LAST:event_lbDashboard1MouseClicked

    private void lbDashboard2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbDashboard2MouseClicked
        showOrder();
    }//GEN-LAST:event_lbDashboard2MouseClicked

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainUI().setVisible(true));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lbCustomer;
    private javax.swing.JLabel lbDashboard;
    private javax.swing.JLabel lbDashboard1;
    private javax.swing.JLabel lbDashboard2;
    private javax.swing.JLabel lbEmployee;
    private javax.swing.JLabel lbInvoice;
    private javax.swing.JLabel lbInvoice1;
    private javax.swing.JLabel lbLogout;
    private javax.swing.JLabel lbName;
    private javax.swing.JLabel ldProduct;
    private javax.swing.JPanel pnContent;
    private javax.swing.JPanel pnMenu;
    // End of variables declaration//GEN-END:variables

}
