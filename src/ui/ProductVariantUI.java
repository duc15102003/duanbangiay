package ui;

import entity.Brand;
import entity.Category;
import entity.Color;
import entity.Product;
import entity.ProductVariant;
import entity.Size;
import entity.filter.ProductVariantFilter;
import java.awt.Image;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import listener.DataChangeListener;
import service.BrandService;
import service.CategoryService;
import service.ColorService;
import service.ProductService;
import service.ProductVariantService;
import service.SizeService;

public class ProductVariantUI extends javax.swing.JPanel {
    
    private ProductVariantService productVariantService = new ProductVariantService();
    private ProductService productService = new ProductService();
    private ColorService colorService = new ColorService();
    private SizeService sizeService = new SizeService();
    private BrandService brandService = new BrandService();
    private CategoryService categoryService = new CategoryService();
    
    private Image originalImage;
    private JDialog imageDialog; 
    private JDialog imagePreviewDialog;
    private JLabel imagePreviewLabel;
    private List<ProductVariant> listProductVariant = new ArrayList<>();
    private List<Product> listProduct = new ArrayList<>();
    private List<Color> listColor = new ArrayList<>();
    private List<Size> listSize = new ArrayList<>();
    private List<Brand> listBrand = new ArrayList<>();
    private List<Category> listCategory = new ArrayList<>();
    private Map<String, ImageIcon> imageCache = new HashMap<>();
    private Map<Integer, ProductVariant> productVariantMap = new HashMap<>();  
    private Map<String, List<Integer>> imageRowMap = new HashMap<>();
    
    private DataChangeListener listener;
    private javax.swing.Timer searchTimer;
    
    private boolean isFilling = false;

    private final java.util.concurrent.ExecutorService imageLoader 
        = java.util.concurrent.Executors.newFixedThreadPool(4);
    
    // ================= FORMAT =================
    private final DecimalFormat moneyFormat;
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        moneyFormat = new DecimalFormat("#,###", symbols);
    }
    
    public ProductVariantUI() {
        initComponents();
        initTable();
        initImagePreview();
        loadCombobox();
        reloadData();

        initSearchListeners();
    }
    
    public ProductVariantUI(DataChangeListener listener) {
        this.listener = listener;
        initComponents();
        initTable();
        initImagePreview();
        loadCombobox();
        reloadData();

        initSearchListeners();
    }
    
    private ProductVariantFilter buildProductFilter(){

        ProductVariantFilter filter = new ProductVariantFilter();

        filter.setSearch(txtSearch.getText().trim());
        filter.setSizeId(getSelectedId(cbbSearchSize));
        filter.setColorId(getSelectedId(cbbSearchColor));
        filter.setBrandId(getSelectedId(cbbSearchBrand));
        filter.setCategoryId(getSelectedId(cbbSearchCategory));
        filter.setProductId(getSelectedId(cbbProduct));

        return filter;
    }
    
    private void initTable(){

        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Ảnh","Mã","Tên","Danh mục","Thương hiệu",
                "Màu sắc","Kích thước","Số lượng tồn",
                "Đơn giá","Trạng thái","Ghi chú", "Id"
            }
        ){
            @Override
            public Class<?> getColumnClass(int column){
                if(column == 0) return Icon.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };

        tblProductVariant.setModel(model);

        tblProductVariant.setRowHeight(90);
        tblProductVariant.getColumnModel().getColumn(0).setPreferredWidth(100);

        // ===== CĂN GIỮA BODY =====
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for(int i = 1; i < tblProductVariant.getColumnCount(); i++){
            tblProductVariant.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // ===== CĂN GIỮA HEADER =====
        JTableHeader header = tblProductVariant.getTableHeader();
        DefaultTableCellRenderer headerRenderer =
            (DefaultTableCellRenderer) header.getDefaultRenderer();

        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }
    
    private void formatMoneyField(){

        String text = txtPrice.getText().replaceAll("[^0-9]", "");

        if(text.isEmpty()){
            txtPrice.setText("");
            return;
        }

        try{

            long value = Long.parseLong(text);

            String formatted = moneyFormat.format(value);

            txtPrice.setText(formatted);

        }catch(Exception e){
            txtPrice.setText("");
        }
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
    
    private void loadCombobox(){

        listProduct  = productService.findAll(null);
        listColor    = colorService.findAll(null);
        listSize     = sizeService.findAll(null);
        listBrand    = brandService.findAll(null); 
        listCategory = categoryService.findAll(null); 

        // combobox form
        loadCombo(cbbProduct, listProduct, " ");
        loadCombo(cbbColor,   listColor,   " ");
        loadCombo(cbbSize,    listSize,    " ");

        // combobox filter/search
        loadCombo(cbbSearchBrand,    listBrand,    "-- Chọn thương hiệu --");
        loadCombo(cbbSearchCategory, listCategory, "-- Chọn danh mục --");
        loadCombo(cbbSearchColor,    listColor,    "-- Chọn màu sắc --");
        loadCombo(cbbSearchSize,     listSize,     "-- Chọn kích thước --");
    }
    
    private void initSearchListeners() {

        java.awt.event.ActionListener searchListener = e -> {
            if (!isFilling) {
                searchTable();
            }
        };

        cbbSearchBrand.addActionListener(searchListener);
        cbbSearchCategory.addActionListener(searchListener);
        cbbSearchColor.addActionListener(searchListener);
        cbbSearchSize.addActionListener(searchListener);
        cbbProduct.addActionListener(searchListener);
    }
    
    private void loadTable(ProductVariantFilter filter){

        listProductVariant = productVariantService.findAll(filter);

        productVariantMap.clear();
        imageRowMap.clear();

        DefaultTableModel model = (DefaultTableModel) tblProductVariant.getModel();
        model.setRowCount(0);

        if(listProductVariant == null || listProductVariant.isEmpty()){
            return;
        }

        for(int i = 0; i < listProductVariant.size(); i++){

            ProductVariant pv = listProductVariant.get(i);

            productVariantMap.put(pv.getId(), pv);

            model.addRow(new Object[]{
                loadImage(pv.getImage()),
                pv.getProductCode(),
                pv.getProductName(),
                pv.getCategoryName(),
                pv.getBrandName(),
                pv.getColorName(),
                pv.getSizeName(),
                pv.getQuantity(),
                moneyFormat.format(pv.getPrice()),
                pv.getStatus(),
                pv.getDescription(),
                pv.getId(),
            });

            imageRowMap
                .computeIfAbsent(pv.getImage(), k -> new ArrayList<>())
                .add(i);
        }

        tblProductVariant.getColumnModel().getColumn(11).setMinWidth(0);
        tblProductVariant.getColumnModel().getColumn(11).setMaxWidth(0);
        tblProductVariant.getColumnModel().getColumn(11).setWidth(0);
    }
    
    private void searchTable(){
        loadTable(buildProductFilter());
    }
    
    private ImageIcon loadImage(String url){

        if(url == null || url.isBlank()) return null;

        ImageIcon cached = imageCache.get(url);
        if(cached != null){
            return cached;
        }

        imageLoader.submit(() -> {
            try{

                ImageIcon icon = new ImageIcon(new URL(url));
                Image scaled = scaleImage(icon.getImage(), 90, 90);
                ImageIcon finalIcon = new ImageIcon(scaled);

                imageCache.put(url, finalIcon);

                javax.swing.SwingUtilities.invokeLater(() -> {

                    List<Integer> rows = imageRowMap.get(url);

                    if(rows != null){
                        for(int row : rows){
                            tblProductVariant.setValueAt(finalIcon, row, 0);
                        }
                    }

                });

            }catch(Exception e){
            }
        });

        return null;
    }
    
    private void fillForm(ProductVariant pv){
        isFilling = true;

        txtPrice.setText(moneyFormat.format(pv.getPrice()));
        spnQuantity.setValue(pv.getQuantity());
        txtImage.setText(pv.getImage());

        showPreview();

        selectComboItem(cbbProduct,pv.getProductId());
        selectComboItem(cbbColor,pv.getColorId());
        selectComboItem(cbbSize,pv.getSizeId());

        isFilling = false;
    }
    
    private ProductVariant getFormData(){

        Product product = (Product) cbbProduct.getSelectedItem();
        Color color = (Color) cbbColor.getSelectedItem();
        Size size = (Size) cbbSize.getSelectedItem();

        ProductVariant pv = new ProductVariant();

        if(product != null) pv.setProductId(product.getId());
        if(color != null) pv.setColorId(color.getId());
        if(size != null) pv.setSizeId(size.getId());

        pv.setQuantity((int) spnQuantity.getValue());

        String price = txtPrice.getText().replace(".","");
        pv.setPrice(Float.parseFloat(price));

        pv.setImage(txtImage.getText());

        return pv;
    }
    
    private Integer getSelectedId(JComboBox<?> combo){

        Object obj = combo.getSelectedItem();

        if(obj == null) return null;

        if(obj instanceof String) return null;

        if(obj instanceof Product p) return p.getId();
        if(obj instanceof Color c) return c.getId();
        if(obj instanceof Size s) return s.getId();
        if(obj instanceof Brand b) return b.getId();
        if(obj instanceof Category c) return c.getId();

        return null;
    }
    
    private <T> void selectComboItem(javax.swing.JComboBox<T> combo,int id){

        for(int i=0;i<combo.getItemCount();i++){

            Object obj = combo.getItemAt(i);

            if(obj instanceof Product p && p.getId()==id){
                combo.setSelectedIndex(i);
                return;
            }

            if(obj instanceof Color c && c.getId()==id){
                combo.setSelectedIndex(i);
                return;
            }

            if(obj instanceof Size s && s.getId()==id){
                combo.setSelectedIndex(i);
                return;
            }
        }
    }
    
    private void initImagePreview() {

        imagePreviewDialog = new JDialog();
        imagePreviewDialog.setUndecorated(true);

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePreviewLabel.setVerticalAlignment(JLabel.CENTER);

        imagePreviewDialog.add(imagePreviewLabel);
        imagePreviewDialog.setSize(600,600);
    }
    
    private Image scaleImage(Image img, int width, int height){

        java.awt.image.BufferedImage buffered =
                new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        java.awt.Graphics2D g2 = buffered.createGraphics();

        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                java.awt.RenderingHints.VALUE_RENDER_QUALITY);

        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(img, 0, 0, width, height, null);

        g2.dispose();

        return buffered;
    }
    
    private void showPreview(){

        String url = txtImage.getText().trim();

        txtImage.setToolTipText(url);

        if(url.isEmpty()){
            originalImage = null;
            lbImagePreview.setIcon(null);
            lbImagePreview.setText("");
            return;
        }

        try{

            ImageIcon icon = imageCache.get(url);

            if(icon == null){
                icon = new ImageIcon(new URL(url));
                imageCache.put(url, icon);
            }

            originalImage = icon.getImage();

            Image preview = scaleImage(originalImage,90,90);

            lbImagePreview.setText("");
            lbImagePreview.setIcon(new ImageIcon(preview));

        }
        catch(Exception e){

            originalImage = null;
            lbImagePreview.setIcon(null);
            lbImagePreview.setText("Sai URL");
        }
    }
    
    private void refreshForm(){

        //cbbProduct.setSelectedIndex(0);
        cbbColor.setSelectedIndex(0);
        cbbSize.setSelectedIndex(0);

        spnQuantity.setValue(0);

        txtPrice.setText("");
        txtImage.setText("");

        originalImage = null;

        lbImagePreview.setIcon(null);
        lbImagePreview.setText("");

        tblProductVariant.clearSelection();
        
        txtSearch.setText("");
        cbbSearchBrand.setSelectedIndex(0);
        cbbSearchCategory.setSelectedIndex(0);
        cbbSearchColor.setSelectedIndex(0);
        cbbSearchSize.setSelectedIndex(0);
    }
    
    public void reloadData(){
        refreshForm();

        loadTable(buildProductFilter());
    }
    
    public void selectProduct(int productId, String productName) {
        Integer targetId = productId;

        listProduct = productService.findAll(null);      
        loadCombo(cbbProduct, listProduct, " ");   

        for (int i = 0; i < cbbProduct.getItemCount(); i++) {
            Object item = cbbProduct.getItemAt(i);
            if (item instanceof Product p && p.getId() == targetId) {
                cbbProduct.setSelectedItem(p);
                break;
            }
        }
        
        ProductVariantFilter filter = new ProductVariantFilter();
        filter.setProductId(targetId);

        loadTable(filter);
    }
    
    public void reloadAllData() {
        try {
            listProduct  = productService.findAll(null);
            listColor    = colorService.findAll(null);
            listSize     = sizeService.findAll(null);
            listBrand    = brandService.findAll(null); 
            listCategory = categoryService.findAll(null); 

            loadCombo(cbbProduct, listProduct, " ");
            loadCombo(cbbColor,   listColor,   " ");
            loadCombo(cbbSize,    listSize,    " ");

            loadCombo(cbbSearchBrand,    listBrand,    "-- Chọn thương hiệu --");
            loadCombo(cbbSearchCategory, listCategory, "-- Chọn danh mục --");
            loadCombo(cbbSearchColor,    listColor,    "-- Chọn màu sắc --");
            loadCombo(cbbSearchSize,     listSize,     "-- Chọn kích thước --");

            refreshForm();
            imageCache.clear();
            loadTable(buildProductFilter());

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

        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        cbbProduct = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        cbbColor = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        cbbSize = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        spnQuantity = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        txtPrice = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        btnInsert = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductVariant = new javax.swing.JTable();
        lbImagePreview = new javax.swing.JLabel();
        txtImage = new javax.swing.JTextField();
        cbbSearchColor = new javax.swing.JComboBox<>();
        cbbSearchSize = new javax.swing.JComboBox<>();
        cbbSearchCategory = new javax.swing.JComboBox<>();
        cbbSearchBrand = new javax.swing.JComboBox<>();

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Sản phẩm biến thể");

        jLabel1.setText("Sản phẩm");

        jLabel2.setText("Màu sắc");

        jLabel4.setText("Kích thước");

        jLabel5.setText("Số lượng");

        jLabel6.setText("Đơn giá");

        txtPrice.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPrice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPriceKeyReleased(evt);
            }
        });

        jLabel7.setText("Ảnh");

        btnInsert.setText("Thêm");
        btnInsert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertActionPerformed(evt);
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

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchKeyReleased(evt);
            }
        });

        tblProductVariant.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ảnh", "Mã", "Tên", "Màu sắc", "Kích thước", "Số lượng", "Đơn giá"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProductVariant.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductVariantMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProductVariant);

        txtImage.setMaximumSize(new java.awt.Dimension(222, 2147483647));
        txtImage.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtImageKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cbbSearchBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbbSearchCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbbSearchSize, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbbSearchColor, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 198, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(123, 123, 123)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(57, 57, 57)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(195, 195, 195))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(cbbSize, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addComponent(cbbProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbbColor, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(96, 96, 96)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtPrice)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(lbImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(spnQuantity))))
                .addGap(173, 173, 173))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel3)
                .addGap(52, 52, 52)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(cbbProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(21, 21, 21)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7)
                            .addComponent(txtImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(spnQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(lbImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsert)
                    .addComponent(btnRefresh)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete))
                .addGap(41, 41, 41)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtImageKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtImageKeyReleased
        showPreview();
    }//GEN-LAST:event_txtImageKeyReleased

    private void tblProductVariantMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductVariantMouseClicked
        int row = tblProductVariant.getSelectedRow();
        if(row < 0) return;

        int id = (int) tblProductVariant.getValueAt(row, 11);
        ProductVariant pv = productVariantMap.get(id);

        if(pv != null){
            fillForm(pv);
        }
    }//GEN-LAST:event_tblProductVariantMouseClicked

    private void txtSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchKeyReleased
        if(searchTimer != null){
            searchTimer.stop();
        }

        searchTimer = new javax.swing.Timer(300, e -> searchTable());
        searchTimer.setRepeats(false);
        searchTimer.start();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertActionPerformed
        try {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn thêm sản phẩm này?",
                    "Xác nhận thêm",
                    JOptionPane.YES_NO_OPTION
            );

            if(confirm != JOptionPane.YES_OPTION) return;

            ProductVariant pv = getFormData();

            boolean result = productVariantService.create(pv);

            if(result){
                JOptionPane.showMessageDialog(this,"Thêm thành công");
                reloadData();
            } else {
                JOptionPane.showMessageDialog(this,"Thêm thất bại");
            }

        } catch(Exception e){
            JOptionPane.showMessageDialog(this,"Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnInsertActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int row = tblProductVariant.getSelectedRow();

        if(row < 0){
            JOptionPane.showMessageDialog(this,"Chọn sản phẩm cần sửa");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn cập nhật sản phẩm này?",
                "Xác nhận cập nhật",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{
            int id = (int) tblProductVariant.getValueAt(row, 11);
            ProductVariant pvOld = productVariantMap.get(id);

            if(pvOld == null) return;

            ProductVariant pv = getFormData();
            pv.setId(pvOld.getId());

            boolean result = productVariantService.update(pv);

            if(result){
                JOptionPane.showMessageDialog(this,"Cập nhật thành công");
                reloadData();
            }else{
                JOptionPane.showMessageDialog(this,"Cập nhật thất bại");
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,"Có lỗi xảy ra: " + e.getMessage());
            e.printStackTrace();
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int row = tblProductVariant.getSelectedRow();

        if(row < 0){
            JOptionPane.showMessageDialog(this,"Chọn sản phẩm cần xoá");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xoá?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if(confirm != JOptionPane.YES_OPTION) return;

        try{

            int id = (int) tblProductVariant.getValueAt(row, 11);
            ProductVariant pv = productVariantMap.get(id);

            if(pv == null) return;

            boolean result = productVariantService.delete(pv.getId());

            if(result){
                JOptionPane.showMessageDialog(this,"Xoá thành công");
                reloadData();
            }else{
                JOptionPane.showMessageDialog(this,"Xoá thất bại");
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshForm();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void txtPriceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPriceKeyReleased
        formatMoneyField();
    }//GEN-LAST:event_txtPriceKeyReleased
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnInsert;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<Object> cbbColor;
    private javax.swing.JComboBox<Object> cbbProduct;
    private javax.swing.JComboBox<Object> cbbSearchBrand;
    private javax.swing.JComboBox<Object> cbbSearchCategory;
    private javax.swing.JComboBox<Object> cbbSearchColor;
    private javax.swing.JComboBox<Object> cbbSearchSize;
    private javax.swing.JComboBox<Object> cbbSize;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbImagePreview;
    private javax.swing.JSpinner spnQuantity;
    private javax.swing.JTable tblProductVariant;
    private javax.swing.JTextField txtImage;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
