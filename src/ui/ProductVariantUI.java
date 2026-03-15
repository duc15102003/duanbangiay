package ui;

import entity.Brand;
import entity.Category;
import entity.Color;
import entity.Product;
import entity.ProductVariant;
import entity.Size;
import entity.filter.ProductVariantFilter;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JFrame;
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
    
    private DataChangeListener listener;

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
        enableTableImageHover();
        loadCombobox();
        reloadData();
    }
    
    public ProductVariantUI(DataChangeListener listener) {
        initComponents();
        initTable();
        initImagePreview();
        enableTableImageHover();
        loadCombobox();
        reloadData();
    }
    
   private void reloadSizeCombo(){
        listSize = sizeService.findAll(null);
        loadCombo(cbbSize, listSize, " ");
    }

    private void reloadColorCombo(){
        listColor = colorService.findAll(null);
        loadCombo(cbbColor, listColor, " ");
    }
    
    private ProductVariantFilter buildProductFilter(){

        ProductVariantFilter filter = new ProductVariantFilter();

        filter.setSearch(txtSearch.getText().trim());
        filter.setSizeId(getSelectedId(cbbSearchSize));
        filter.setColorId(getSelectedId(cbbSearchColor));
        filter.setBrandId(getSelectedId(cbbSearchBrand));
        filter.setCategoryId(getSelectedId(cbbSearchCategory));

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
    
    private void loadTable(ProductVariantFilter filter){

        listProductVariant = productVariantService.findAll(filter);

        productVariantMap.clear();

        DefaultTableModel model = (DefaultTableModel) tblProductVariant.getModel();
        model.setRowCount(0);

        if(listProductVariant == null || listProductVariant.isEmpty()){
            return;
        }

        for(ProductVariant pv : listProductVariant){

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

                    for(int i = 0; i < tblProductVariant.getRowCount(); i++){

                        Object value = tblProductVariant.getValueAt(i, 0);

                        if(value == null){

                            String code = tblProductVariant.getValueAt(i,1).toString();
                            ProductVariant pv = productVariantMap.get(code);

                            if(pv != null && url.equals(pv.getImage())){
                                tblProductVariant.setValueAt(finalIcon, i, 0);
                            }
                        }
                    }

                });

            }catch(Exception e){
            }
        });

        return null;
    }
    
    private void fillForm(ProductVariant pv){

        txtPrice.setText(moneyFormat.format(pv.getPrice()));

        spnQuantity.setValue(pv.getQuantity());

        txtImage.setText(pv.getImage());

        showPreview();

        selectComboItem(cbbProduct,pv.getProductId());
        selectComboItem(cbbColor,pv.getColorId());
        selectComboItem(cbbSize,pv.getSizeId());
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

        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePreviewLabel.setVerticalAlignment(JLabel.CENTER);

        enableImagePreview(lbImagePreview);
    }
    
    private void enableImagePreview(JLabel label){

        label.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseEntered(MouseEvent e){

                if(originalImage == null) return;

                Image scaled = scaleImage(originalImage,600,600);

                imagePreviewLabel.setIcon(new ImageIcon(scaled));

                imagePreviewDialog.setSize(600,600);

                imagePreviewDialog.setLocationRelativeTo(null);

                imagePreviewDialog.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e){

                imagePreviewDialog.setVisible(false);
            }
        });
    }
    
    private void enableTableImageHover(){

        tblProductVariant.addMouseMotionListener(new MouseAdapter(){

            @Override
            public void mouseMoved(MouseEvent e){

                int row = tblProductVariant.rowAtPoint(e.getPoint());
                int col = tblProductVariant.columnAtPoint(e.getPoint());

                if(row < 0 || col != 0){
                    imagePreviewDialog.setVisible(false);
                    return;
                }

                String code = tblProductVariant.getValueAt(row,1).toString();

                for(ProductVariant pv : listProductVariant){

                    if(pv.getProductCode().equals(code)){

                        try{

                            ImageIcon icon = new ImageIcon(new URL(pv.getImage()));

                            Image scaled = scaleImage(icon.getImage(),600,600);

                            imagePreviewLabel.setIcon(new ImageIcon(scaled));

                            imagePreviewLabel.setIcon(new ImageIcon(scaled));

                            imagePreviewDialog.setSize(600,600);
                            imagePreviewDialog.setLocationRelativeTo(null);

                            imagePreviewDialog.setVisible(true);

                        }catch(Exception ex){
                            imagePreviewDialog.setVisible(false);
                        }

                        break;
                    }
                }
            }
        });

        tblProductVariant.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseExited(MouseEvent e){
                imagePreviewDialog.setVisible(false);
            }
        });
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

            ImageIcon icon = new ImageIcon(new URL(url));

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

        cbbProduct.setSelectedIndex(0);
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
    
    private void reloadProductCombo(){

        listProduct = productService.findAll(null);

        loadCombo(cbbProduct, listProduct, " ");
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
        btnOpenPopupProduct = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
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

        btnOpenPopupProduct.setText("+");
        btnOpenPopupProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenPopupProductActionPerformed(evt);
            }
        });

        jButton2.setText("+");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("+");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

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
                .addGap(123, 123, 123)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnInsert, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(109, 109, 109)
                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(105, 105, 105)
                        .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbbProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbbColor, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(cbbSize, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnOpenPopupProduct)
                            .addComponent(jButton2)
                            .addComponent(jButton3))
                        .addGap(118, 118, 118)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel6))
                                .addGap(18, 18, 18))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(spnQuantity)
                            .addComponent(txtPrice)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(txtImage, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lbImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(115, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
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
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
                            .addComponent(cbbProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOpenPopupProduct))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jButton2))
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(jButton3)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(spnQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtPrice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbImagePreview, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel7)
                                .addComponent(txtImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsert)
                    .addComponent(btnRefresh)
                    .addComponent(btnUpdate)
                    .addComponent(btnDelete))
                .addGap(31, 31, 31)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSearchBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
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
        searchTable();
    }//GEN-LAST:event_txtSearchKeyReleased

    private void btnInsertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertActionPerformed
        try{

            ProductVariant pv = getFormData();

            boolean result = productVariantService.create(pv);

            if(result){
                JOptionPane.showMessageDialog(this,"Thêm thành công");
                reloadData();
            }else{
                JOptionPane.showMessageDialog(this,"Thêm thất bại");
            }

        }catch(Exception e){
            JOptionPane.showMessageDialog(this,e.getMessage());
        }
    }//GEN-LAST:event_btnInsertActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
            int row = tblProductVariant.getSelectedRow();

            if(row < 0){
                JOptionPane.showMessageDialog(this,"Chọn sản phẩm cần sửa");
                return;
            }

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
            JOptionPane.showMessageDialog(this,e.getMessage());
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

    private void btnOpenPopupProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenPopupProductActionPerformed
        JFrame frame = new JFrame("Quản lý sản phẩm");

        ProductUI productUI = new ProductUI(() -> {
            reloadProductCombo();
        });

        frame.setContentPane(productUI);

        frame.setSize(603, 684);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setVisible(true);
    }//GEN-LAST:event_btnOpenPopupProductActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JFrame frame = new JFrame("Quản lý màu sắc");

        ColorUI colorUI = new ColorUI(() -> {
            reloadColorCombo();
        });

        frame.setContentPane(colorUI);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        JFrame frame = new JFrame("Quản lý kích thước");

        SizeUI sizeUI = new SizeUI(() -> {
            reloadColorCombo();
        });

        frame.setContentPane(sizeUI);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnInsert;
    private javax.swing.JButton btnOpenPopupProduct;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox<String> cbbColor;
    private javax.swing.JComboBox<String> cbbProduct;
    private javax.swing.JComboBox<String> cbbSearchBrand;
    private javax.swing.JComboBox<String> cbbSearchCategory;
    private javax.swing.JComboBox<String> cbbSearchColor;
    private javax.swing.JComboBox<String> cbbSearchSize;
    private javax.swing.JComboBox<String> cbbSize;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
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
