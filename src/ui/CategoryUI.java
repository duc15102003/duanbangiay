package ui;

import entity.Category;
import entity.filter.CategoryFilter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import listener.DataChangeListener;
import service.CategoryService;

public class CategoryUI extends javax.swing.JPanel {

        private CategoryService categoryService = new CategoryService();
        private List<Category> listCategory = new ArrayList<>();
        
        private Category selectedCategory = null;
        
        private DataChangeListener listener;


        public CategoryUI(DataChangeListener listener) {
            this.listener = listener;
            initComponents();
            loadCategory();
        }

        private void renderTable(List<Category> data){

            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            model.setRowCount(0);

            for(Category c : data){
                model.addRow(new Object[]{
                    c.getCode(),
                    c.getName()
                });
            }
        }

        private void loadCategory(){

            CategoryFilter filter = new CategoryFilter();

            listCategory = categoryService.findAll(filter);

            renderTable(listCategory);
        }

        private Category getFormData(){

            Category c = new Category();

            c.setCode(txtCode.getText().trim());
            c.setName(txtCode1.getText().trim());

            return c;
        }

        private void clearForm(){

            txtCode.setText("");
            txtCode1.setText("");

            selectedCategory = null;

            jTable2.clearSelection();
        }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        btnDel = new javax.swing.JButton();
        btnRefr = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnUpd = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCode = new javax.swing.JTextField();
        txtCode1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Mã danh mục", "Tên danh mục"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable2);

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

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        btnUpd.setText("Sửa");
        btnUpd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdActionPerformed(evt);
            }
        });

        btnAdd.setText("Thêm");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        jLabel1.setText("Mã danh mục");

        jLabel2.setText("Tên danh mục");

        txtCode.setEnabled(false);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Danh mục");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(240, 240, 240)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnAdd)
                        .addGap(69, 69, 69)
                        .addComponent(btnUpd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addComponent(btnDel)
                        .addGap(44, 44, 44)
                        .addComponent(btnRefr))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(77, 77, 77)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCode, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
                            .addComponent(txtCode1))))
                .addGap(0, 294, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel3)
                .addGap(57, 57, 57)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCode1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(66, 66, 66)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdd)
                    .addComponent(btnRefr)
                    .addComponent(btnUpd)
                    .addComponent(btnDel))
                .addGap(18, 18, 18)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        if(selectedCategory == null){
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục cần xoá!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có chắc muốn xoá danh mục này?", 
            "Xác nhận xoá", 
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(categoryService.delete(selectedCategory.getId())){
            JOptionPane.showMessageDialog(this, "Xoá thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadCategory();
            clearForm();
            if(listener != null){
                listener.onDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Xoá thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnRefrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefrActionPerformed
        clearForm();
        loadCategory();
    }//GEN-LAST:event_btnRefrActionPerformed

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        CategoryFilter filter = new CategoryFilter();
        filter.setSearch(txtSearch.getText().trim());

        listCategory = categoryService.findAll(filter);

        renderTable(listCategory);
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnUpdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdActionPerformed
        if(selectedCategory == null){
            JOptionPane.showMessageDialog(this, "Vui lòng chọn danh mục cần sửa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có chắc muốn cập nhật danh mục này?", 
            "Xác nhận sửa", 
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        Category c = getFormData();
        c.setId(selectedCategory.getId());

        if(categoryService.update(c)){
            JOptionPane.showMessageDialog(this, "Cập nhật danh mục thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadCategory();
            clearForm();
            if(listener != null){
                listener.onDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        Category c = getFormData();

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Bạn có chắc muốn thêm danh mục này?", 
            "Xác nhận thêm", 
            JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        if(categoryService.insert(c)){
            JOptionPane.showMessageDialog(this, "Thêm danh mục thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadCategory();
            clearForm();
            if(listener != null){
                listener.onDataChanged();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại! Kiểm tra lại dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        int row = jTable2.getSelectedRow();

        if(row < 0) return;

        selectedCategory = listCategory.get(row);

        txtCode.setText(selectedCategory.getCode());
        txtCode1.setText(selectedCategory.getName());
    }//GEN-LAST:event_jTable2MouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnRefr;
    private javax.swing.JButton btnUpd;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField txtCode;
    private javax.swing.JTextField txtCode1;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
