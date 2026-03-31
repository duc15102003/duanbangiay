package ui;

import entity.Brand;
import entity.Category;
import entity.Product;
import entity.filter.ProductFilter;
import enums.ProductStatusEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import listener.DataChangeListener;
import service.BrandService;
import service.CategoryService;
import service.ProductService;

public class ProductUI extends javax.swing.JPanel  {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ProductUI.class.getName());
    
    private ProductService productService = new ProductService();
    private CategoryService categoryService = new CategoryService();
    private BrandService brandService = new BrandService();

    private List<Category> categories = new ArrayList<>();
    private List<Brand> brands = new ArrayList<>();
    
    private javax.swing.Timer searchTimer;
    
    private Map<Integer, Integer> quantityCache = new HashMap<>();

    private DataChangeListener listener;
    
    private ProductManagerUI parentManager;

    public ProductUI() {
        initComponents();
        initData();
        initFilterEvent();
        reloadAllData();
    }
    
    public ProductUI(ProductManagerUI manager) {
        this.parentManager = manager;
        initComponents();
        initData();
        reloadAllData();
        initFilterEvent();
    }

    public ProductUI(ProductManagerUI manager, DataChangeListener listener) {
        this.parentManager = manager;
        this.listener = listener;
        initComponents();
        initData();
        initFilterEvent();
    }

    private void initData() {

        loadStatus(cbbStatus, false);
        loadStatus(cbbSearchStatus, true);

        loadCategory();
        loadBrand();

        loadProductTable();
    }
    
    private void loadStatus(JComboBox combo, boolean hasPlaceholder){

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        if(hasPlaceholder){
            model.addElement("-- Chọn trạng thái --");
        }

        for(ProductStatusEnum s : ProductStatusEnum.values()){
            model.addElement(s);
        }

        combo.setModel(model);
    }
    
    private void loadCategory(){

        categories = categoryService.findAll(null);

        loadCombo(cbbCategory, categories, "");
        loadCombo(cbbSearchCategory, categories, "-- Chọn danh mục --");
    }
    
    private void loadBrand(){

        brands = brandService.findAll(null);

        loadCombo(cbbBrand, brands, "");
        loadCombo(cbbSearchBrand, brands, "-- Chọn thương hiệu --");
    }
    
    private Integer getSelectedId(JComboBox combo){

        Object obj = combo.getSelectedItem();

        if(obj instanceof Category c) return c.getId();
        if(obj instanceof Brand b) return b.getId();

        return null;
    }
    
    private void filterProduct(){

        ProductFilter filter = new ProductFilter();

        filter.setSearch(jTextField1.getText().trim());
        filter.setBrandId(getSelectedId(cbbSearchBrand));
        filter.setCategoryId(getSelectedId(cbbSearchCategory));

        Object status = cbbSearchStatus.getSelectedItem();

        if(status instanceof ProductStatusEnum s){
            filter.setStatus(s);
        }

        List<Product> list = productService.findAll(filter);
        
        renderTable(list);
    }
    
    private Product getFormData(){

        Product p = new Product();

        p.setCode(txtMaSP.getText().trim());
        p.setName(txtTenSP.getText().trim());
        p.setDescription(txtGhiChu.getText().trim());

        p.setCategoryId(getSelectedId(cbbCategory));
        p.setBrandId(getSelectedId(cbbBrand));

        Object status = cbbStatus.getSelectedItem();

        if(status instanceof ProductStatusEnum s){
            p.setStatus(s);
        }

        return p;
    }
    
    private void loadCombo(JComboBox combo, List<?> data, Object placeholder){

        combo.removeAllItems();
        combo.addItem(placeholder);

        if(data == null) return;

        for(Object item : data){
            combo.addItem(item);
        }

        combo.setSelectedIndex(0);
    }

    private void initFilterEvent() {
        cbbSearchBrand.addActionListener(e -> filterProduct());
        cbbSearchCategory.addActionListener(e -> filterProduct());
        cbbSearchStatus.addActionListener(e -> filterProduct());
    }
    
    private void renderTable(List<Product> list){

        DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
        model.setRowCount(0);

        if(list == null || list.isEmpty()){
            return;
        }

        for(Product p : list){

            Integer quantity = quantityCache.get(p.getId());

            if(quantity == null){
                quantity = productService.countVariants(p.getId());
                quantityCache.put(p.getId(), quantity);
            }

            model.addRow(new Object[]{
                p.getCode(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getStatus().getLabel(),
                quantity,
                p.getDescription(),
                p.getId()
            });
        }

        jTable3.getColumnModel().getColumn(7).setMinWidth(0);
        jTable3.getColumnModel().getColumn(7).setMaxWidth(0);
    }

    private void fillForm(int row) {

        txtMaSP.setText(jTable3.getValueAt(row, 0).toString());
        txtTenSP.setText(jTable3.getValueAt(row, 1).toString());

        String categoryName = jTable3.getValueAt(row, 2).toString();
        String brandName = jTable3.getValueAt(row, 3).toString();
        String label = jTable3.getValueAt(row, 4).toString();

        // set category
        for(Category c : categories){
            if(c.getName().equals(categoryName)){
                cbbCategory.setSelectedItem(c);
                break;
            }
        }

        for(Brand b : brands){
            if(b.getName().equals(brandName)){
                cbbBrand.setSelectedItem(b);
                break;
            }
        }

        for(ProductStatusEnum s : ProductStatusEnum.values()){
            if(s.getLabel().equals(label)){
                cbbStatus.setSelectedItem(s);
                break;
            }
        }

        Object quantityObj = jTable3.getValueAt(row, 5);
        txtQuantity.setText(quantityObj == null ? "" : quantityObj.toString());

        // set ghi chú
        txtGhiChu.setText(
            jTable3.getValueAt(row, 6) == null ? "" : jTable3.getValueAt(row, 6).toString()
        );
    }

    private void refreshData() {

        txtMaSP.setText("");
        txtTenSP.setText("");
        txtGhiChu.setText("");

        cbbCategory.setSelectedIndex(0);
        cbbBrand.setSelectedIndex(0);
        cbbStatus.setSelectedIndex(0);

        filterProduct();
        
        cbbSearchBrand.setSelectedIndex(0);
        cbbSearchCategory.setSelectedIndex(0);
        cbbSearchStatus.setSelectedIndex(0);
        jTextField1.setText("");
        
        reloadAllData();
    }

    private void loadProductTable(){
        filterProduct();
    }
   
    public void reloadAllData() {
        try {

            if(categories == null || categories.isEmpty()){
                categories = categoryService.findAll(null);
            }

            if(brands == null || brands.isEmpty()){
                brands = brandService.findAll(null);
            }

            loadCombo(cbbCategory, categories, "");
            loadCombo(cbbBrand, brands, "");
            loadStatus(cbbStatus, false);

            loadCombo(cbbSearchCategory, categories, "-- Chọn danh mục --");
            loadCombo(cbbSearchBrand, brands, "-- Chọn thương hiệu --");
            loadStatus(cbbSearchStatus, true);

            quantityCache.clear();

            loadProductTable();

            txtMaSP.setText("");
            txtTenSP.setText("");
            txtGhiChu.setText("");
            txtQuantity.setText("");

            cbbCategory.setSelectedIndex(0);
            cbbBrand.setSelectedIndex(0);
            cbbStatus.setSelectedIndex(0);

            cbbSearchBrand.setSelectedIndex(0);
            cbbSearchCategory.setSelectedIndex(0);
            cbbSearchStatus.setSelectedIndex(0);
            jTextField1.setText("");

            if(listener != null) {
                listener.onDataChanged();
            }

        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải lại dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtTenSP = new javax.swing.JTextField();
        cbbBrand = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        cbbStatus = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtGhiChu = new javax.swing.JTextArea();
        btnAdd = new javax.swing.JButton();
        btnUpd = new javax.swing.JButton();
        btnRefr = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        txtMaSP = new javax.swing.JTextField();
        btnDel = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        cbbSearchBrand = new javax.swing.JComboBox<>();
        cbbSearchCategory = new javax.swing.JComboBox<>();
        cbbSearchStatus = new javax.swing.JComboBox<>();
        cbbCategory = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable2);

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel3.setText("Sản phẩm");

        jLabel1.setText("Mã sản phẩm:");

        jLabel2.setText("Tên sản phẩm:");

        jLabel4.setText("Ghi chú:");

        jLabel5.setText("Danh mục:");

        jLabel6.setText("Thương hiệu:");

        jLabel7.setText("Trạng thái:");

        txtGhiChu.setColumns(20);
        txtGhiChu.setRows(5);
        jScrollPane1.setViewportView(txtGhiChu);

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

        btnRefr.setText("Làm mới");
        btnRefr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefrActionPerformed(evt);
            }
        });

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã", "Tên", "Danh mục", "Thương hiệu", "Trạng thái", "Số lượng", "Ghi chú", "Id"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(jTable3);

        btnDel.setText("Xoá");
        btnDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDelActionPerformed(evt);
            }
        });

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel8.setText("Số lượng");

        txtQuantity.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cbbSearchStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cbbSearchCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cbbSearchBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField1))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(282, 282, 282)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addGap(56, 56, 56)
                                .addComponent(txtQuantity, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(48, 48, 48)
                                    .addComponent(btnUpd, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(48, 48, 48)
                                    .addComponent(btnDel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(48, 48, 48)
                                    .addComponent(btnRefr))
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jLabel1)
                                            .addGap(28, 28, 28))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel2)
                                                .addComponent(jLabel5)
                                                .addComponent(jLabel6)
                                                .addComponent(jLabel7))
                                            .addGap(26, 26, 26)))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(cbbStatus, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(txtTenSP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                                        .addComponent(txtMaSP, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(cbbCategory, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(cbbBrand, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 282, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(430, 430, 430))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel3)
                .addGap(33, 33, 33)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtMaSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtTenSP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbbBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAdd)
                            .addComponent(btnUpd)
                            .addComponent(btnDel)
                            .addComponent(btnRefr))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSearchBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSearchCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSearchStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
        txtMaSP.setEnabled(false);
        int row = jTable3.getSelectedRow();
        fillForm(row);

        if (evt.getClickCount() == 2 && row != -1) {
            int productId = Integer.parseInt(jTable3.getValueAt(row, 7).toString());
            String productName = jTable3.getValueAt(row, 1).toString();

            if (parentManager != null) {
                parentManager.getTabbedPane().setSelectedIndex(2);
                parentManager.getProductVariantUI().selectProduct(productId, productName);
            }
        }
    }//GEN-LAST:event_jTable3MouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        try {

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn thêm sản phẩm?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if(confirm != JOptionPane.YES_OPTION) return;

            Product p = getFormData();

            boolean result = productService.create(p);

            if(result){
                JOptionPane.showMessageDialog(this,"Thêm sản phẩm thành công!");
                loadProductTable();
                refreshData();
                if(listener != null){
                    listener.onDataChanged();
                }
            }else{
                JOptionPane.showMessageDialog(this,"Thêm sản phẩm thất bại!");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnUpdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdActionPerformed
        int row = jTable3.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn sản phẩm cần sửa!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn cập nhật sản phẩm?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{

            Product p = getFormData();

            int id = Integer.parseInt(
                jTable3.getValueAt(row,7).toString()
            );

            p.setId(id);

            boolean result = productService.update(p);

            if(result){
                JOptionPane.showMessageDialog(this,"Cập nhật thành công!");
                loadProductTable();
                if(listener != null){
                    listener.onDataChanged();
                }
            }else{
                JOptionPane.showMessageDialog(this,"Cập nhật thất bại!");
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnUpdActionPerformed

    private void btnDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDelActionPerformed
        int row = jTable3.getSelectedRow();

        if(row == -1){
            JOptionPane.showMessageDialog(this,"Vui lòng chọn sản phẩm cần xoá!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xoá sản phẩm này?",
                "Xác nhận xoá",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{

            int id = Integer.parseInt(
                jTable3.getValueAt(row, 7).toString()
            );

            boolean result = productService.delete(id);

            if(result){
                JOptionPane.showMessageDialog(this,"Xoá thành công!");
                loadProductTable();
                if(listener != null){
                    listener.onDataChanged();
                }
            }else{
                JOptionPane.showMessageDialog(this,"Xoá thất bại!");
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnDelActionPerformed

    private void btnRefrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefrActionPerformed
        refreshData();
        txtMaSP.enable(true);
    }//GEN-LAST:event_btnRefrActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        if(searchTimer != null){
            searchTimer.stop();
        }

        searchTimer = new javax.swing.Timer(300, e -> filterProduct());
        searchTimer.setRepeats(false);
        searchTimer.start();
    }//GEN-LAST:event_jTextField1KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDel;
    private javax.swing.JButton btnRefr;
    private javax.swing.JButton btnUpd;
    private javax.swing.JComboBox<String> cbbBrand;
    private javax.swing.JComboBox<String> cbbCategory;
    private javax.swing.JComboBox<String> cbbSearchBrand;
    private javax.swing.JComboBox<String> cbbSearchCategory;
    private javax.swing.JComboBox<String> cbbSearchStatus;
    private javax.swing.JComboBox<String> cbbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextArea txtGhiChu;
    private javax.swing.JTextField txtMaSP;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtTenSP;
    // End of variables declaration//GEN-END:variables
}
