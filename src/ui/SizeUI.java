package ui;

import entity.Size;
import entity.filter.SizeFilter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import listener.DataChangeListener;
import service.SizeService;

public class SizeUI extends javax.swing.JPanel {

    private SizeService sizeService = new SizeService();
    private List<Size> listSize = new ArrayList<>();

    private Size selectedSize = null;

    private DataChangeListener listener;

    public SizeUI(DataChangeListener listener) {
        this.listener = listener;
        initComponents();
        loadSize();
        
        txtName.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();

                if (!Character.isDigit(c)) {
                    evt.consume();
                }
            }
        });
    }

    //Load data vào table
    private void renderTable(List<Size> data){

        DefaultTableModel model = (DefaultTableModel) tblSize.getModel();
        model.setRowCount(0);

        for(Size s : data){
            model.addRow(new Object[]{
                s.getCode(),
                s.getName()
            });
        }
    }

    private void loadSize(){

        SizeFilter filter = new SizeFilter();

        listSize = sizeService.findAll(filter);

        renderTable(listSize);
    }

    private Size getFormData(){

        Size s = new Size();

        s.setCode(txtCode.getText().trim());
        s.setName(txtName.getText().trim());

        return s;
    }

    private void clearForm(){

        txtCode.setText("");
        txtName.setText("");

        selectedSize = null;

        tblSize.clearSelection();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblSize = new javax.swing.JTable();
        btnUpd = new javax.swing.JButton();
        btnDel = new javax.swing.JButton();
        btnRefr = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtCode = new javax.swing.JTextField();

        tblSize.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Mã kích thước", "Tên kích thước"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblSize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblSizeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblSize);

        btnUpd.setText("Sửa");
        btnUpd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdActionPerformed(evt);
            }
        });

        btnDel.setText("Xoá");
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

        btnAdd.setText("Thêm");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Kích thước");

        jLabel2.setText("Mã kích thước");

        jLabel3.setText("Tên kích thước");

        txtCode.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1019, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(240, 240, 240)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAdd)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                                .addComponent(btnUpd)
                                .addGap(88, 88, 88)
                                .addComponent(btnDel)
                                .addGap(59, 59, 59)
                                .addComponent(btnRefr))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(2, 2, 2))
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtCode, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                                    .addComponent(txtName))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 290, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addGap(57, 57, 57)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefr)
                    .addComponent(btnDel)
                    .addComponent(btnUpd)
                    .addComponent(btnAdd))
                .addGap(27, 27, 27)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblSizeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblSizeMouseClicked
        int row = tblSize.getSelectedRow();

        if(row < 0) return;

        selectedSize = listSize.get(row);

        txtCode.setText(selectedSize.getCode());
        txtName.setText(selectedSize.getName());
    }//GEN-LAST:event_tblSizeMouseClicked

    private void btnUpdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdActionPerformed
        if(selectedSize == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn cập nhật kích thước này?",
                "Xác nhận cập nhật",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        Size s = getFormData();
        s.setId(selectedSize.getId());

        if(sizeService.update(s)){
            JOptionPane.showMessageDialog(this,"Cập nhật thành công!");
            loadSize();
            clearForm();
            if(listener != null){
                listener.onDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this,"Cập nhật thất bại!");
        }
    }//GEN-LAST:event_btnUpdActionPerformed

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        if(selectedSize == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xoá kích thước này?",
                "Xác nhận xoá",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(sizeService.delete(selectedSize.getId())){
            JOptionPane.showMessageDialog(this,"Xoá thành công!");
            loadSize();
            clearForm();
            if(listener != null){
                listener.onDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this,"Xoá thất bại!");
        }
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnRefrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefrActionPerformed
        clearForm();
        loadSize();
    }//GEN-LAST:event_btnRefrActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Size s = getFormData();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thêm kích thước này?",
                "Xác nhận thêm",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(sizeService.insert(s)){
            JOptionPane.showMessageDialog(this,"Thêm thành công!");
            loadSize();
            clearForm();
            if(listener != null){
                listener.onDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this,"Thêm thất bại!");
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        SizeFilter filter = new SizeFilter();
        filter.setSearch(txtSearch.getText().trim());

        listSize = sizeService.findAll(filter);

        renderTable(listSize);
    }//GEN-LAST:event_txtSearchKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnRefr;
    private javax.swing.JButton btnUpd;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblSize;
    private javax.swing.JTextField txtCode;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
