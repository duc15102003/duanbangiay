package ui;

import entity.Customer;
import entity.filter.CustomerFilter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import service.CustomerService;

public class CustomerUI extends javax.swing.JPanel {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CustomerUI.class.getName());
    
    private OrderUI orderUI;
    private boolean selectMode = false;
    
    // ================= SERVICES =================
    private CustomerService customerService = new CustomerService();

    // ================= DATA =================
    private List<Customer> listCustomer = new ArrayList<>();

    public CustomerUI() {
        initComponents();

        btnSelectCustomer.setVisible(false);
        initCustomer();
    }
    
    public CustomerUI(OrderUI orderUI, boolean selectMode) {
        initComponents();

        this.orderUI = orderUI;
        this.selectMode = selectMode;

        btnSelectCustomer.setVisible(selectMode);

        initCustomer();
    }
    
    private CustomerFilter buildCustomerFilter(){

        CustomerFilter filter = new CustomerFilter();

        filter.setSearch(txtSearch.getText());

        return filter;
    }
    
    private void initCustomer(){

        CustomerFilter filter = buildCustomerFilter();

        listCustomer = customerService.findAll(filter);

        centerTableHeader(tblCustomer);

        renderCustomer(tblCustomer,listCustomer);

        if(selectMode){
            addTableSelectionListener();
        }
    }
    
    private void centerTableHeader(JTable table){

        // Center Header
        DefaultTableCellRenderer headerRenderer =
            (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();

        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        // Center Body
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        int columnCount = table.getColumnModel().getColumnCount();

        for(int i = 0; i < columnCount; i++){
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }
    
    private void renderCustomer(JTable table,List<Customer> items){

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0);

        for(Customer c : items){

            model.addRow(buildCustomerRow(c));

        }
    }
    
    private Object[] buildCustomerRow(Customer c){

        return new Object[]{
            c.getId(),
            c.getName(),
            c.getPhone(),
            c.getEmail(),
            c.getDateOfBirth(),
            c.getGender() ? "Nam" : "Nữ",
            c.getStatus() != null ? c.getStatus().getLabel() : "",
            c.getAddress()
        };
    }
    
    private void addTableSelectionListener() {

        tblCustomer.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int row = tblCustomer.getSelectedRow();

                btnSelectCustomer.setEnabled(row != -1);

            }

        });
    }  
    
    private Customer getFormData(){

        Customer c = new Customer();

        c.setCode(txtCode.getText().trim());
        c.setName(txtName.getText().trim());
        c.setPhone(txtPhone.getText().trim());
        c.setEmail(txtMail.getText().trim());
        c.setAddress(txtAddress.getText().trim());

        if(dcBirth.getDate() != null){
            c.setDateOfBirth(new java.sql.Date(dcBirth.getDate().getTime()));
        }

        c.setGender(rdoMale.isSelected());

        if(rdoActive.isSelected()){
            c.setStatus(enums.CustomerStatusEnum.ACTIVE);
        }else if(rdoInactive.isSelected()){
            c.setStatus(enums.CustomerStatusEnum.INACTIVE);
        }

        return c;
    }
    
    private void fillForm(int row){

        Customer c = listCustomer.get(row);

        txtCode.setText(c.getCode());
        txtName.setText(c.getName());
        txtPhone.setText(c.getPhone());
        txtMail.setText(c.getEmail());
        txtAddress.setText(c.getAddress());

        if(c.getDateOfBirth() != null){
            dcBirth.setDate(c.getDateOfBirth());
        }else{
            dcBirth.setDate(null);
        }

        if(Boolean.TRUE.equals(c.getGender())){
            rdoMale.setSelected(true);
        }else{
            rdoFemale.setSelected(true);
        }

        if(c.getStatus() == enums.CustomerStatusEnum.ACTIVE){
            rdoActive.setSelected(true);
        }else{
            rdoInactive.setSelected(true);
        }
    }
    
    public void resetForm(){

        txtCode.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtMail.setText("");
        txtAddress.setText("");
        txtSearch.setText("");

        dcBirth.setDate(null);

        buttonGroup1.clearSelection();
        buttonGroup2.clearSelection();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        dcBirth = new com.toedter.calendar.JDateChooser();
        jLabel8 = new javax.swing.JLabel();
        rdoMale = new javax.swing.JRadioButton();
        rdoFemale = new javax.swing.JRadioButton();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAddress = new javax.swing.JTextArea();
        rdoActive = new javax.swing.JRadioButton();
        rdoInactive = new javax.swing.JRadioButton();
        txtName = new javax.swing.JTextField();
        txtMail = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblCustomer = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        btnSelectCustomer = new javax.swing.JButton();
        txtCode = new javax.swing.JTextField();
        txtPhone = new javax.swing.JTextField();

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel3.setText("Sản phẩm");

        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Khách hàng");

        jLabel1.setText("Mã khách hàng ");

        jLabel2.setText("Tên khách hàng ");

        jLabel5.setText("Số điện thoại");

        jLabel6.setText("Mail");

        jLabel7.setText("Ngày sinh");

        jLabel8.setText("Giới tính");

        buttonGroup1.add(rdoMale);
        rdoMale.setText("Nam");

        buttonGroup1.add(rdoFemale);
        rdoFemale.setText("Nữ");

        jLabel9.setText("Trạng thái");

        txtAddress.setColumns(20);
        txtAddress.setRows(5);
        jScrollPane1.setViewportView(txtAddress);

        buttonGroup2.add(rdoActive);
        rdoActive.setText("Hoạt động");

        buttonGroup2.add(rdoInactive);
        rdoInactive.setText("Ngừng hoạt động");

        jLabel10.setText("Địa chỉ");

        btnAdd.setText("Thêm");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpdate.setText("Sửa");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnDelete.setText("Xoá");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnRefresh.setText("Làm mới");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        tblCustomer.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã", "Tên", "SDT", "Mail", "Ngày sinh", "Giới tính", "Trạng thái", "Địa chỉ"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCustomer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCustomerMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblCustomer);

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnSelectCustomer.setText("Chọn khách hàng");
        btnSelectCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectCustomerActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 438, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSelectCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(rdoActive)
                                        .addGap(82, 82, 82)
                                        .addComponent(rdoInactive))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtPhone, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                        .addComponent(dcBirth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtCode)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 195, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel8))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(rdoMale)
                                        .addGap(83, 83, 83)
                                        .addComponent(rdoFemale))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(txtName, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                                        .addComponent(txtMail))))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(207, 207, 207)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 219, Short.MAX_VALUE)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(214, 214, 214)
                        .addComponent(btnRefresh)))
                .addGap(72, 72, 72))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel4)
                .addGap(57, 57, 57)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rdoMale)
                        .addComponent(rdoFemale))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(txtMail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(dcBirth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rdoActive)
                        .addComponent(jLabel9))
                    .addComponent(rdoInactive))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addGap(37, 37, 37)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectCustomer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectCustomerActionPerformed
        int row = tblCustomer.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn khách hàng");
            return;
        }

        int modelRow = tblCustomer.convertRowIndexToModel(row);

        Customer c = listCustomer.get(modelRow);

        if(orderUI != null){
            orderUI.setSelectedCustomer(
                c.getId(),
                c.getName(),
                c.getPhone(),
                c.getAddress()
            );
        }
        
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if(window != null){
            window.dispose();
        }       
    }//GEN-LAST:event_btnSelectCustomerActionPerformed

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        initCustomer();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void tblCustomerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCustomerMouseClicked
        int row = tblCustomer.getSelectedRow();

        if(row != -1){
            int modelRow = tblCustomer.convertRowIndexToModel(row);
            fillForm(modelRow);
        }
    }//GEN-LAST:event_tblCustomerMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        try{

        Customer c = getFormData();

        boolean result = customerService.insert(c);

        if(result){
            JOptionPane.showMessageDialog(this,"Thêm khách hàng thành công");
            initCustomer();
            resetForm();
        }else{
            JOptionPane.showMessageDialog(this,"Thêm thất bại");
        }

    }catch(Exception e){
        JOptionPane.showMessageDialog(this,"Lỗi: " + e.getMessage());
        e.printStackTrace();
    }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int row = tblCustomer.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn khách hàng cần sửa");
            return;
        }

        int modelRow = tblCustomer.convertRowIndexToModel(row);

        Customer c = getFormData();
        c.setId(listCustomer.get(modelRow).getId());

        boolean result = customerService.update(c);

        if(result){
            JOptionPane.showMessageDialog(this,"Cập nhật thành công");
            initCustomer();
            resetForm();
        }else{
            JOptionPane.showMessageDialog(this,"Cập nhật thất bại");
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int row = tblCustomer.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn khách hàng cần xoá");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xoá?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        int modelRow = tblCustomer.convertRowIndexToModel(row);

        int id = listCustomer.get(modelRow).getId();

        boolean result = customerService.delete(id);

        if(result){
            JOptionPane.showMessageDialog(this,"Xoá thành công");
            initCustomer();
            resetForm();
        }else{
            JOptionPane.showMessageDialog(this,"Xoá thất bại");
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        resetForm();
    }//GEN-LAST:event_btnRefreshActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSelectCustomer;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private com.toedter.calendar.JDateChooser dcBirth;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton rdoActive;
    private javax.swing.JRadioButton rdoFemale;
    private javax.swing.JRadioButton rdoInactive;
    private javax.swing.JRadioButton rdoMale;
    private javax.swing.JTable tblCustomer;
    private javax.swing.JTextArea txtAddress;
    private javax.swing.JTextField txtCode;
    private javax.swing.JTextField txtMail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
