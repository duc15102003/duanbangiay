package ui;

import entity.Discount;
import entity.filter.DiscountFilter;
import enums.DiscountStatusEnum;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import service.DiscountService;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;

public class DiscountUI extends javax.swing.JPanel {
    
    private DiscountService discountService = new DiscountService();
    private DefaultTableModel model;
    private List<Discount> discounts;
    private int selectedId = -1;
    
    private final DecimalFormat moneyFormat = new java.text.DecimalFormat("#,###");

    public DiscountUI() {
        initComponents();
        
        formatMoney(txtDiscountValue);
        formatMoney(txtMaximumDiscount);
        
        DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        moneyFormat.setDecimalFormatSymbols(symbols);
        
        model = (DefaultTableModel) tblDiscount.getModel();
        loadStatus();
        loadTable();
        
         checkDiscountType();
         formatDate();
         
        dcStartedAt.getDateEditor().addPropertyChangeListener("date", evt -> loadTable());
        dcEndedAt.getDateEditor().addPropertyChangeListener("date", evt -> loadTable());
        
        javax.swing.JTextField txtFromEditor = 
            (javax.swing.JTextField) dcStartedAt.getDateEditor().getUiComponent();

        javax.swing.JTextField txtToEditor = 
            (javax.swing.JTextField) dcEndedAt.getDateEditor().getUiComponent();

        txtFromEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e){ loadTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ loadTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ loadTable(); }
        });

        txtToEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e){ loadTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ loadTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ loadTable(); }
        });
    }
    
    private void formatDate(){
        dcFrom.setDateFormatString("dd-MM-yyyy");
        dcTo.setDateFormatString("dd-MM-yyyy");
        dcStartedAt.setDateFormatString("dd-MM-yyyy");
        dcEndedAt.setDateFormatString("dd-MM-yyyy");
    }
    
    private void loadStatus(){

        cbbStatus.removeAllItems();

        for(DiscountStatusEnum s : DiscountStatusEnum.values()){
            cbbStatus.addItem(s.getLabel());
        }
    }
    
    private void loadTable() {
        DiscountFilter filter = new DiscountFilter();
        filter.setSearch(txtSearch.getText());

        Date from = dcStartedAt.getDate();
        Date to = dcEndedAt.getDate();

        if (from != null) filter.setFromStartedAt(LocalDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault()));
        if (to != null) filter.setToEndedAt(LocalDateTime.ofInstant(to.toInstant(), ZoneId.systemDefault()));

        discounts = discountService.findAll(filter);

        model.setRowCount(0);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (Discount d : discounts) {
            model.addRow(new Object[]{
                d.getCode(),                                  
                d.getDiscountType(),                             
                moneyFormat.format(d.getDiscountValue()),   
                d.getMaximumDiscount() != null ? moneyFormat.format(d.getMaximumDiscount()) : "",
                d.getStartedAt() != null ? d.getStartedAt().format(f) : "",
                d.getEndedAt() != null ? d.getEndedAt().format(f) : "",     
                d.getQuantity(),                                
                d.getDiscountCondition(),                       
                d.getStatus().getLabel()                      
            });
        }
    }
    
    private void formatMoney(javax.swing.JTextField txt){
        txt.addKeyListener(new java.awt.event.KeyAdapter(){
            @Override
            public void keyReleased(java.awt.event.KeyEvent e){
                String value = txt.getText().replace(".", "").replaceAll("[^0-9]", "");
                if(value.isEmpty()) return;

                try{
                    long number = Long.parseLong(value);
                    txt.setText(moneyFormat.format(number));
                }catch(Exception ex){
                }
            }
        });
    }
    
    private Discount getForm() {
        Discount d = new Discount();

        d.setId(selectedId);
        d.setCode(txtCode.getText().trim());
        d.setDiscountType(cbbDiscountType.getSelectedItem().toString());

        String discountValueStr = txtDiscountValue.getText().replace(".", "").trim();
        if (discountValueStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giá trị giảm không được để trống");
            return null;
        }
        d.setDiscountValue(Integer.parseInt(discountValueStr));

        String maxStr = txtMaximumDiscount.getText().replace(".", "").trim();
        if (!maxStr.isEmpty()) {
            d.setMaximumDiscount(Integer.parseInt(maxStr));
        } else {
            d.setMaximumDiscount(null);
        }

        String qtyStr = txtQuantity.getText().trim();
        if (!qtyStr.isEmpty()) {
            try {
                d.setQuantity(Integer.parseInt(qtyStr));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số lượng phải là số nguyên!");
                return null;
            }
        } else {
            d.setQuantity(0);
        }

        int condition = Integer.parseInt(txtDiscountCondition.getText().trim());
        d.setDiscountCondition(condition);

        Date from = dcFrom.getDate();
        Date to = dcTo.getDate();

        if (from != null) {
            d.setStartedAt(LocalDateTime.ofInstant(from.toInstant(), ZoneId.systemDefault()));
        }

        if (to != null) {
            d.setEndedAt(LocalDateTime.ofInstant(to.toInstant(), ZoneId.systemDefault()));
        }

        int statusIndex = cbbStatus.getSelectedIndex();
        d.setStatus(DiscountStatusEnum.values()[statusIndex]);

        return d;
    }
    
    public void refreshForm(){
        txtCode.setText("");
        txtDiscountValue.setText("");
        txtMaximumDiscount.setText("");
        txtSearch.setText("");

        dcFrom.setDate(null);
        dcTo.setDate(null);
                
        dcStartedAt.setDate(null);
        dcEndedAt.setDate(null);

        selectedId = -1;
        txtCode.enable(true);

        loadTable();
    }
    
    private void checkDiscountType() {
        String type = (String) cbbDiscountType.getSelectedItem();

        if ("%".equals(type)) {
            txtMaximumDiscount.setEnabled(true);
        } else {
            txtMaximumDiscount.setEnabled(false);
            txtMaximumDiscount.setText("");
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtDiscountValue = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtMaximumDiscount = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        dcFrom = new com.toedter.calendar.JDateChooser();
        jLabel8 = new javax.swing.JLabel();
        dcTo = new com.toedter.calendar.JDateChooser();
        jLabel9 = new javax.swing.JLabel();
        cbbStatus = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDiscount = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        btnUpd = new javax.swing.JButton();
        btnDel = new javax.swing.JButton();
        btnRefr = new javax.swing.JButton();
        dcEndedAt = new com.toedter.calendar.JDateChooser();
        dcStartedAt = new com.toedter.calendar.JDateChooser();
        cbbDiscountType = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtDiscountCondition = new javax.swing.JTextArea();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Phiếu giảm giá");

        jLabel2.setText("Mã");

        jLabel4.setText("Loại giảm giá");

        jLabel5.setText("Giá trị giảm");

        jLabel6.setText("Giá trị giảm tối đa");

        jLabel7.setText("Từ ngày");

        jLabel8.setText("Đến ngày");

        jLabel9.setText("Trạng thái");

        tblDiscount.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã", "Loại giảm giá", "Giá trị giảm", "Giá trị giảm tối đa", "Từ ngày", "Đến ngày", "Số lượng", "Điều kiện giảm", "Trạng thái"
            }
        ));
        tblDiscount.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDiscountMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblDiscount);

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnAdd.setText("Thêm");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        btnUpd.setText("Sửa");
        btnUpd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdActionPerformed(evt);
            }
        });

        btnDel.setText("Xóa");
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });

        btnRefr.setText("Làm mới");
        btnRefr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefrActionPerformed(evt);
            }
        });

        dcEndedAt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dcEndedAtKeyReleased(evt);
            }
        });

        dcStartedAt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                dcStartedAtKeyReleased(evt);
            }
        });

        cbbDiscountType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tiền mặt", "%" }));
        cbbDiscountType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbDiscountTypeActionPerformed(evt);
            }
        });

        jLabel3.setText("Đến");

        jLabel10.setText("Từ");

        jLabel11.setText("Số lượng");

        jLabel12.setText("Điều kiện giảm");

        txtDiscountCondition.setColumns(20);
        txtDiscountCondition.setRows(5);
        jScrollPane2.setViewportView(txtDiscountCondition);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDel)
                        .addGap(158, 158, 158)
                        .addComponent(btnRefr))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(11, 11, 11)
                                .addComponent(jScrollPane2))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(72, 72, 72)
                                .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel11))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbbDiscountType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtDiscountValue, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                                    .addComponent(txtQuantity))))
                        .addGap(158, 158, 158)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel9))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtMaximumDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE)
                                .addComponent(dcFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(dcTo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbbStatus, 0, 239, Short.MAX_VALUE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(393, 393, 393)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnUpd)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(dcStartedAt, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(57, 57, 57)
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(dcEndedAt, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1)
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtMaximumDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(cbbDiscountType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtDiscountValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5)))))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUpd)
                    .addComponent(btnDel)
                    .addComponent(btnRefr)
                    .addComponent(btnAdd))
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(dcStartedAt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dcEndedAt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblDiscountMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDiscountMouseClicked
        txtCode.enable(false);
        int row = tblDiscount.getSelectedRow();
        if (row < 0) return;

        Discount d = discounts.get(row);
        selectedId = d.getId();

        txtCode.setText(d.getCode());
        txtDiscountValue.setText(moneyFormat.format(d.getDiscountValue()));
        txtMaximumDiscount.setText(d.getMaximumDiscount() != null ? moneyFormat.format(d.getMaximumDiscount()) : "");
        txtQuantity.setText(String.valueOf(d.getQuantity()));
        txtDiscountCondition.setText(String.valueOf(d.getDiscountCondition()));

        cbbDiscountType.setSelectedItem(d.getDiscountType());
        cbbStatus.setSelectedItem(d.getStatus().getLabel());

        if (d.getStartedAt() != null) dcFrom.setDate(Date.from(d.getStartedAt().atZone(ZoneId.systemDefault()).toInstant()));
        else dcFrom.setDate(null);

        if (d.getEndedAt() != null) dcTo.setDate(Date.from(d.getEndedAt().atZone(ZoneId.systemDefault()).toInstant()));
        else dcTo.setDate(null);
    }//GEN-LAST:event_tblDiscountMouseClicked

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        loadTable();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Discount d = getForm();
        if(d == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn thêm phiếu giảm giá này?",
            "Xác nhận thêm",
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(discountService.insert(d)){
            JOptionPane.showMessageDialog(this, "Thêm thành công");
            loadTable();
            refreshForm();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại");
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdActionPerformed
        Discount d = getForm();
        if(d == null) return; 

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn cập nhật phiếu giảm giá này?",
            "Xác nhận sửa",
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(discountService.update(d)){
            JOptionPane.showMessageDialog(this, "Cập nhật thành công");
            loadTable();
            refreshForm();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại");
        }
    }//GEN-LAST:event_btnUpdActionPerformed

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        if(selectedId == -1) return;

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xóa phiếu giảm giá này?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(discountService.delete(selectedId)){
            JOptionPane.showMessageDialog(this, "Xóa thành công");
            loadTable();
            refreshForm();
        } else {
            JOptionPane.showMessageDialog(this, "Xóa thất bại");
        }
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnRefrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefrActionPerformed
        refreshForm();
    }//GEN-LAST:event_btnRefrActionPerformed

    private void dcStartedAtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dcStartedAtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_dcStartedAtKeyReleased

    private void dcEndedAtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_dcEndedAtKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_dcEndedAtKeyReleased

    private void cbbDiscountTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbDiscountTypeActionPerformed
        checkDiscountType();
    }//GEN-LAST:event_cbbDiscountTypeActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnRefr;
    private javax.swing.JButton btnUpd;
    private javax.swing.JComboBox<String> cbbDiscountType;
    private javax.swing.JComboBox<String> cbbStatus;
    private com.toedter.calendar.JDateChooser dcEndedAt;
    private com.toedter.calendar.JDateChooser dcFrom;
    private com.toedter.calendar.JDateChooser dcStartedAt;
    private com.toedter.calendar.JDateChooser dcTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
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
    private javax.swing.JTable tblDiscount;
    private javax.swing.JTextField txtCode;
    private javax.swing.JTextArea txtDiscountCondition;
    private javax.swing.JTextField txtDiscountValue;
    private javax.swing.JTextField txtMaximumDiscount;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
