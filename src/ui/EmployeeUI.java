package ui;

import entity.Employee;
import entity.filter.EmployeeFilter;
import enums.RoleEnum;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import service.EmployeeService;

public class EmployeeUI extends javax.swing.JPanel {

    // ================= SERVICES =================
    private EmployeeService employeeService = new EmployeeService();

    // ================= DATA =================
    private List<Employee> listEmployee = new ArrayList<>();
    
    public EmployeeUI() {
        initComponents();
        initEmployee();
        loadRole();
    }
    
    private void loadRole(){

        DefaultComboBoxModel model = new DefaultComboBoxModel<>();

        for(RoleEnum r : RoleEnum.values()){
            model.addElement(r);
        }

        cbbRole.setModel(model);
    }
    
    private void initEmployee(){

        EmployeeFilter filter = buildEmployeeFilter();

        listEmployee = employeeService.findAll(filter);

        centerTableHeader(tblEmployee);

        renderEmployee(tblEmployee, listEmployee);
    }
    
    private void centerTableHeader(JTable table){

        DefaultTableCellRenderer headerRenderer =
            (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();

        headerRenderer.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        int columnCount = table.getColumnModel().getColumnCount();

        for(int i = 0; i < columnCount; i++){
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }
    
    private void renderEmployee(JTable table, List<Employee> items){

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        model.setRowCount(0);

        for(Employee e : items){
            model.addRow(buildEmployeeRow(e));
        }
    }
    
    private Object[] buildEmployeeRow(Employee e){

        return new Object[]{
            e.getCode(),
            e.getName(),
            e.getUsername(),
            e.getPhone(),
            e.getEmail(),
            e.getPassword(),
            e.getDateOfBirth(),
            e.getGender() ? "Nam" : "Nữ",
            e.getRole() != null ? e.getRole().getLabel() : "",
            e.getAddress()
        };
    }
    
    private Employee getFormData(){

        Employee e = new Employee();

        e.setCode(txtCode.getText().trim());
        e.setName(txtName.getText().trim());
        e.setUsername(txtUsername.getText().trim());
        e.setPassword(txtPass.getText().trim());
        e.setPhone(txtPhone.getText().trim());
        e.setEmail(txtMail.getText().trim());
        e.setAddress(taAddress.getText().trim());

        if(dcBirth.getDate() != null){
            e.setDateOfBirth(new java.sql.Date(dcBirth.getDate().getTime()));
        }

        e.setGender(rdoMale.isSelected());

        RoleEnum role = RoleEnum.values()[cbbRole.getSelectedIndex()];
        e.setRole(role);

        return e;
    }
    
    private void fillForm(int row){

        Employee e = listEmployee.get(row);

        txtCode.setText(e.getCode());
        txtName.setText(e.getName());
        txtUsername.setText(e.getUsername());
        txtPhone.setText(e.getPhone());
        txtMail.setText(e.getEmail());
        txtPass.setText(e.getPassword());
        taAddress.setText(e.getAddress());

        if(e.getDateOfBirth() != null){
            dcBirth.setDate(e.getDateOfBirth());
        }

        if(Boolean.TRUE.equals(e.getGender())){
            rdoMale.setSelected(true);
        }else{
            rdoFemale.setSelected(true);
        }

        if(e.getRole() != null){
            cbbRole.setSelectedItem(e.getRole());
        }
    }
    
    public void resetForm(){

        txtCode.setText("");
        txtName.setText("");
        txtUsername.setText("");
        txtPhone.setText("");
        txtMail.setText("");
        taAddress.setText("");
        txtSearch.setText("");
        txtPass.setText("");

        dcBirth.setDate(null);

        buttonGroup1.clearSelection();

        cbbRole.setSelectedIndex(0);
    }
    
    private EmployeeFilter buildEmployeeFilter(){

        EmployeeFilter filter = new EmployeeFilter();

        filter.setSearch(txtSearch.getText());

        return filter;
    }
    
    private boolean validateDateFormat() {
        String dateStr = ((javax.swing.JTextField) dcBirth.getDateEditor().getUiComponent())
                            .getText().trim();

        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày sinh");
            return false;
        }

        java.text.SimpleDateFormat sdf = 
            new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH);

        sdf.setLenient(false);

        try {
            sdf.parse(dateStr);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ngày sinh phải đúng định dạng: MM dd, yyyy");
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        txtName = new javax.swing.JTextField();
        txtCode = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cbbRole = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txtMail = new javax.swing.JTextField();
        dcBirth = new com.toedter.calendar.JDateChooser();
        jLabel11 = new javax.swing.JLabel();
        rdoMale = new javax.swing.JRadioButton();
        rdoFemale = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taAddress = new javax.swing.JTextArea();
        btnAdd = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblEmployee = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txtPass = new javax.swing.JTextField();

        jLabel5.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Nhân viên");

        jLabel2.setText("Mã nhân viên ");

        jLabel3.setText("Tên nhân viên ");

        jLabel6.setText("Tên đăng nhập");

        jLabel7.setText("Số điện thoại");

        jLabel8.setText("Mail");

        jLabel9.setText("Chức vụ");

        jLabel10.setText("Ngày sinh");

        jLabel11.setText("Giới tính");

        buttonGroup1.add(rdoMale);
        rdoMale.setText("Nam");

        buttonGroup1.add(rdoFemale);
        rdoFemale.setText("Nữ");

        jLabel12.setText("Địa chỉ");

        taAddress.setColumns(20);
        taAddress.setRows(5);
        jScrollPane1.setViewportView(taAddress);

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

        tblEmployee.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã", "Tên", "Tên đăng nhập", "SDT", "Mail", "Mật khẩu", "Ngày sinh", "Giới tính", "Chức vụ", "Địa chỉ"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblEmployee.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblEmployeeMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblEmployee);

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        jLabel13.setText("Mật khẩu");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(72, 72, 72)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 228, Short.MAX_VALUE)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(206, 206, 206)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(214, 214, 214)
                        .addComponent(btnRefresh))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel6)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                            .addComponent(jLabel7)
                            .addComponent(jLabel13))
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtCode)
                                .addComponent(txtName)
                                .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE))
                            .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 314, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addComponent(jLabel8)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtMail, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                            .addComponent(dcBirth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane1)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(rdoMale, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(rdoFemale, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(cbbRole, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(72, 72, 72))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel5)
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)
                        .addComponent(jLabel10))
                    .addComponent(dcBirth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbRole, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(rdoMale)
                    .addComponent(rdoFemale))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel12)
                        .addComponent(jLabel13)
                        .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(43, 43, 43)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete)
                    .addComponent(btnRefresh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        try{
            Employee e = getFormData();
            
            if (!validateDateFormat()) {
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thêm nhân viên này?",
                "Xác nhận thêm",
                JOptionPane.YES_NO_OPTION
            );

            if(confirm != JOptionPane.YES_OPTION) return;

            boolean result = employeeService.insert(e);

            if(result){
                JOptionPane.showMessageDialog(this,"Thêm nhân viên thành công");
                initEmployee();
                resetForm();
            }else{
                JOptionPane.showMessageDialog(this,"Thêm thất bại");
            }

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,"Lỗi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int row = tblEmployee.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa");
            return;
        }
        
        if (!validateDateFormat()) {
                return;
        }

        int modelRow = tblEmployee.convertRowIndexToModel(row);
        Employee e = getFormData();
        e.setId(listEmployee.get(modelRow).getId());

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn cập nhật nhân viên này?",
            "Xác nhận sửa",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            boolean result = employeeService.update(e);

            if (result) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công");
                initEmployee();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
       int row = tblEmployee.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn nhân viên cần xoá");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xoá?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        int modelRow = tblEmployee.convertRowIndexToModel(row);

        int id = listEmployee.get(modelRow).getId();

        boolean result = employeeService.delete(id);

        if(result){
            JOptionPane.showMessageDialog(this,"Xoá thành công");
            initEmployee();
            resetForm();
        }else{
            JOptionPane.showMessageDialog(this,"Xoá thất bại");
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        resetForm();
        txtCode.enable(true);
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void tblEmployeeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblEmployeeMouseClicked
        txtCode.enable(false);
        int row = tblEmployee.getSelectedRow();

        if(row != -1){
            int modelRow = tblEmployee.convertRowIndexToModel(row);
            fillForm(modelRow);
        }
    }//GEN-LAST:event_tblEmployeeMouseClicked

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        initEmployee();
    }//GEN-LAST:event_txtSearchKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbbRole;
    private com.toedter.calendar.JDateChooser dcBirth;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JRadioButton rdoFemale;
    private javax.swing.JRadioButton rdoMale;
    private javax.swing.JTextArea taAddress;
    private javax.swing.JTable tblEmployee;
    private javax.swing.JTextField txtCode;
    private javax.swing.JTextField txtMail;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtPass;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
