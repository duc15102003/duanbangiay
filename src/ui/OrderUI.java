package ui;

import entity.Brand;
import entity.Category;
import entity.Color;
import entity.Invoice;
import entity.InvoiceItem;
import entity.ProductVariant;
import entity.Size;
import entity.filter.InvoiceFilter;
import entity.filter.ProductVariantFilter;
import enums.OrderStatusEnum;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import service.BrandService;
import service.CartService;
import service.CategoryService;
import service.ColorService;
import service.InvoiceService;
import service.ProductVariantService;
import service.SizeService;

public class OrderUI extends JFrame {
    
    private static final Logger logger = Logger.getLogger(OrderUI.class.getName());
    
    // ================= SERVICES =================
    private ProductVariantService productVariantService = new ProductVariantService();
    private SizeService sizeService = new SizeService();
    private ColorService colorService = new ColorService();
    private BrandService brandService = new BrandService();
    private CategoryService categoryService = new CategoryService();
    private InvoiceService invoiceService = new InvoiceService();
    private CartService cartService = new CartService();

    // ================= UI SUPPORT =================
    private JDialog imagePreviewDialog;
    private JLabel imagePreviewLabel;
    private Map<String, ImageIcon> imageCache = new HashMap<>();

    // ================= DATA =================
    private List<Invoice> listInvoice = new ArrayList<>();
    private List<ProductVariant> listProductVariant = new ArrayList<>();
    private Map<Integer, List<InvoiceItem>> cartData = new HashMap<>();
    private Map<Integer, List<InvoiceItem>> cartFilteredData = new HashMap<>();
    
    //Thông tin id người mua được chọn và id người bán hàng
    private int selectedCustomerId = 1;
    private int employeeId;

    // ================= FORMAT =================
    private final DecimalFormat moneyFormat;
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        moneyFormat = new DecimalFormat("#,###", symbols);
    }
    
    public OrderUI() {
        initComponents();
        setLocationRelativeTo(null);
        
        initData();
    }
    
    public OrderUI(int userId) {
        this.employeeId = userId;
        initComponents();
        setLocationRelativeTo(null);
        
        initData();
    }
    
    private void initData() {
        initImagePreview();
        initFilterCombobox();
        initFilterEvents();

        jTabbedPane1.addChangeListener(e -> updateTotalAmount());

        initProduct();
        initCart();
    }
    
    private void initImagePreview() {

        imagePreviewDialog = new JDialog(this);
        imagePreviewDialog.setUndecorated(true);

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePreviewLabel.setVerticalAlignment(JLabel.CENTER);

        imagePreviewDialog.add(imagePreviewLabel);
        imagePreviewDialog.setSize(600, 600);

        enableImagePreview(tblProduct);
    }
    
    // ================= PRODUCT =================
    private ProductVariantFilter buildProductFilter(){

        ProductVariantFilter filter = new ProductVariantFilter();

        filter.setSearch(txtSearchProduct.getText());
        filter.setSizeId(getSelectedId(cbbSizeProduct));
        filter.setColorId(getSelectedId(cbbColorProduct));
        filter.setBrandId(getSelectedId(cbbBrandProduct));
        filter.setCategoryId(getSelectedId(cbbCategoryProduct));

        return filter;
    }

    private void initProduct(){

        ProductVariantFilter filter = buildProductFilter();

        listProductVariant = productVariantService.findAll(filter);

        JTable table = createProductTable(listProductVariant);

        jScrollPane5.setViewportView(table);

        tblProduct = table;
    }

    private Object[] buildProductRow(ProductVariant pv){

        return new Object[]{
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
            pv.getDescription()
        };
    }
    
    // ================= PRODUCT TABLE =================
    private JTable createProductTable(List<ProductVariant> items) {

        JTable table = new JTable(
            new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Ảnh","Mã","Tên sản phẩm","Danh mục","Thương hiệu",
                    "Màu sắc","Kích thước","Số lượng tồn",
                    "Đơn giá","Trạng thái","Ghi chú"
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
            }
        );

        table.setRowHeight(100);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        // double click event
        table.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent evt){
                if(evt.getClickCount()==2){
                    tblProductMouseClicked(evt);
                }
            }
        });

        applyProductColumnSize(table);
        applyTableStyle(table);
        centerTableHeader(table);

        renderProduct(table,items);
        enableImagePreview(table);

        return table;
    }
    
    private void renderProduct(JTable table, List<ProductVariant> items){

        clearTable(table);

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        for(ProductVariant pv : items){
            model.addRow(buildProductRow(pv));
        }
    }
    
    // ================= CART =================
    private void initCart() {
        
        int selectedTab = jTabbedPane1.getSelectedIndex();

        jTabbedPane1.removeAll();
        cartData.clear();

        InvoiceFilter filter = new InvoiceFilter();
        filter.setStatus(OrderStatusEnum.DRAFT);

        listInvoice = cartService.findAll(filter);

        String keyword = txtCartSearch.getText().trim();
        if(keyword.isBlank()){
            keyword = null;
        }

        for (Invoice invoice : listInvoice) {

            List<InvoiceItem> items =
                    cartService.findByInvoiceId(invoice.getId(), keyword);

            cartData.put(invoice.getId(), items);

            JTable table = createCartTable(items, invoice.getId());

            JScrollPane scroll = new JScrollPane(table);

            scroll.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            );

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scroll, BorderLayout.CENTER);

            jTabbedPane1.addTab(invoice.getCode(), panel);
        }

        if (jTabbedPane1.getTabCount() > 0) {
            jTabbedPane1.setSelectedIndex(
                Math.max(0, Math.min(selectedTab, jTabbedPane1.getTabCount() - 1))
            );
        }

        updateTotalAmount();
}
    
    private void filterCart(){

        JTable table = getCurrentCartTable();
        if(table == null) return;

        int tabIndex = jTabbedPane1.getSelectedIndex();
        if(tabIndex < 0) return;

        int invoiceId = listInvoice.get(tabIndex).getId();

        List<InvoiceItem> source = cartData.get(invoiceId);
        if(source == null) return;

        String keyword = txtCartSearch.getText().trim();

        Integer brandId = getSelectedId(cbbBrandCart);
        Integer categoryId = getSelectedId(cbbCategoryCart);
        Integer sizeId = getSelectedId(cbbSizeCart);
        Integer colorId = getSelectedId(cbbColorCart);

        List<InvoiceItem> filtered = new ArrayList<>();

        for(InvoiceItem item : source){

            boolean match = true;

            if(!matchSearch(item.getProductName(), keyword)){
                match = false;
            }

            if(brandId != null && brandId != item.getBrandId()){
                match = false;
            }

            if(categoryId != null && categoryId != item.getCategoryId()){
                match = false;
            }

            if(sizeId != null && sizeId != item.getSizeId()){
                match = false;
            }

            if(colorId != null && colorId != item.getColorId()){
                match = false;
            }

            if(match){
                filtered.add(item);
            }
        }

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for(InvoiceItem item : filtered){
            model.addRow(buildCartRow(item));
        }
    }
    
    // ================= CART TABLE =================
    private JTable createCartTable(List<InvoiceItem> items, int invoiceId) {

        JTable table = new JTable(
            new DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Ảnh",
                    "Sản phẩm",
                    "Danh mục",
                    "Thương hiệu",
                    "Màu sắc",
                    "Kích thước",
                    "Số lượng",
                    "Đơn giá",
                    "Thành tiền",
                    "Id"
                }
            ){
                @Override
                public Class<?> getColumnClass(int column){
                    if(column == 0) return Icon.class;
                    return Object.class;
                }

                @Override
                public boolean isCellEditable(int row, int column){
                    return false;
                }
            }
        );
        
        //Ẩn cột Id
        table.getColumnModel().getColumn(9).setMinWidth(0);
        table.getColumnModel().getColumn(9).setMaxWidth(0);
        table.getColumnModel().getColumn(9).setWidth(0);
        
        table.setRowHeight(100);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                tblCartMouseClicked(evt);
            }
        });

        applyCartColumnSize(table);
        applyCartTableStyle(table);
        centerTableHeader(table);

        renderCart(table, invoiceId, items);

        enableImagePreview(table);

        return table;
    }
    
    private void applyCartTableStyle(JTable table){

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);

        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(center);

        table.getColumnModel().getColumn(7).setCellRenderer(right);
        table.getColumnModel().getColumn(8).setCellRenderer(right);
    }

    private void renderCart(JTable table, int invoiceId, List<InvoiceItem> items){

        cartFilteredData.put(invoiceId, new ArrayList<>(items));

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for(InvoiceItem item : items){
            model.addRow(buildCartRow(item));
        }

        updateTotalAmount();
    }

    private Object[] buildCartRow(InvoiceItem item){

        float total = item.getQuantity() * item.getPrice();

        return new Object[]{
            loadImage(item.getImage()),
            item.getProductName(),
            item.getCategoryName(),
            item.getBrandName(),
            item.getColorName(),
            item.getSizeName(),
            item.getQuantity(),
            moneyFormat.format(item.getPrice()),
            moneyFormat.format(total),
            item.getProductVariantId()
        };
    }
    
   private void applyCartColumnSize(JTable table){

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(100); // ảnh
        table.getColumnModel().getColumn(1).setPreferredWidth(275); // sản phẩm
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // danh mục
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // thương hiệu
        table.getColumnModel().getColumn(4).setPreferredWidth(90);  // màu
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // size
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // số lượng
        table.getColumnModel().getColumn(7).setPreferredWidth(130); // đơn giá
        table.getColumnModel().getColumn(8).setPreferredWidth(130); // thành tiền
    }
    
    // ================= FILTER =================
    private void initFilterEvents() {

        cbbSizeProduct.addActionListener(e -> initProduct());
        cbbColorProduct.addActionListener(e -> initProduct());
        cbbBrandProduct.addActionListener(e -> initProduct());
        cbbCategoryProduct.addActionListener(e -> initProduct());

        // CART FILTER
        cbbBrandCart.addActionListener(e -> filterCart());
        cbbCategoryCart.addActionListener(e -> filterCart());
        cbbSizeCart.addActionListener(e -> filterCart());
        cbbColorCart.addActionListener(e -> filterCart());
    }
    
    // ================= COMBO BOX =================
    private void initFilterCombobox(){

        List<Size> sizes = sizeService.findAll(null);
        List<Color> colors = colorService.findAll(null);
        List<Brand> brands = brandService.findAll(null);
        List<Category> categories = categoryService.findAll(null);

        loadCombo(cbbSizeProduct, sizes, "- Chọn kích thước -");
        loadCombo(cbbSizeCart, sizes, "- Chọn kích thước -");

        loadCombo(cbbColorProduct, colors, "- Chọn màu sắc -");
        loadCombo(cbbColorCart, colors, "- Chọn màu sắc -");

        loadCombo(cbbBrandProduct, brands, "- Chọn thương hiệu -");
        loadCombo(cbbBrandCart, brands, "- Chọn thương hiệu -");

        loadCombo(cbbCategoryProduct, categories, "- Chọn danh mục -");
        loadCombo(cbbCategoryCart, categories, "- Chọn danh mục -");
    }
    
    // Load data vào combobox
    private <T> void loadCombo(JComboBox combo, List<T> data, String placeholder){

        combo.removeAllItems();

        combo.addItem(placeholder);

        if(data == null) return;

        for(T item : data){
            combo.addItem(item);
        }
    }
    
    // ================= TABLE RENDERER =================
    private void applyTableStyle(JTable table){

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(JLabel.RIGHT);

        // Center
        table.getColumnModel().getColumn(1).setCellRenderer(center);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(center);

        // Number
        table.getColumnModel().getColumn(7).setCellRenderer(right);
        table.getColumnModel().getColumn(8).setCellRenderer(right);
    }
    
    // ================= TABLE COLUMN SIZE =================
    private void applyProductColumnSize(JTable table){

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(100); // ảnh
        table.getColumnModel().getColumn(1).setPreferredWidth(90);  // mã
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // tên
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // danh mục
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // brand
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // color
        table.getColumnModel().getColumn(6).setPreferredWidth(90);  // size
        table.getColumnModel().getColumn(7).setPreferredWidth(120); // tồn
        table.getColumnModel().getColumn(8).setPreferredWidth(130); // giá
    }
    
    // ================= CUSTOMER =================
    public void setSelectedCustomer(Integer id, String name, String phone, String address) {
        jLabel4.setText(name);
        jLabel5.setText(phone);
        lbAddress.setText(address);
        
        this.selectedCustomerId = id;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tblProduct = new javax.swing.JTable();
        jLabel16 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblCart = new javax.swing.JTable();
        btnInsertCart = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        lblTongTien = new javax.swing.JLabel();
        btnPayment = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnSelectCustomer = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        btnDeleteCart = new javax.swing.JButton();
        btnDeleteRowCart = new javax.swing.JButton();
        cbbSizeProduct = new javax.swing.JComboBox<>();
        cbbColorProduct = new javax.swing.JComboBox<>();
        cbbBrandCart = new javax.swing.JComboBox<>();
        cbbCategoryCart = new javax.swing.JComboBox<>();
        txtCartSearch = new javax.swing.JTextField();
        txtSearchProduct = new javax.swing.JTextField();
        cbbCategoryProduct = new javax.swing.JComboBox<>();
        cbbBrandProduct = new javax.swing.JComboBox<>();
        cbbSizeCart = new javax.swing.JComboBox<>();
        cbbColorCart = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lbAddress = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        tblProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Ảnh", "Mã", "Tên sản phẩm", "Danh mục", "Thương hiệu", "Màu sắc", "Kích thước", "Số lượng tồn", "Đơn giá", "Trạng thái", "Ghi chú"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductMouseClicked(evt);
            }
        });
        jScrollPane5.setViewportView(tblProduct);

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel16.setText("Sản phẩm");

        jPanel4.setMaximumSize(new java.awt.Dimension(32767, 20));

        tblCart.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Ảnh", "Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCart.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCartMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(tblCart);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 1181, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 6, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Giỏ hàng 1", jPanel4);

        btnInsertCart.setText("Thêm giỏ hàng");
        btnInsertCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertCartActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel18.setText("Giảm giá");

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTongTien.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTongTien.setText("Tong tien");

        btnPayment.setText("Thanh toán");
        btnPayment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPaymentActionPerformed(evt);
            }
        });

        btnSelectCustomer.setText("Chọn khách hàng");
        btnSelectCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectCustomerActionPerformed(evt);
            }
        });

        jLabel2.setText("Tên khách hàng:");

        jLabel3.setText("Số điện thoại:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        btnDeleteCart.setText("Xoá giỏ hàng");
        btnDeleteCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteCartActionPerformed(evt);
            }
        });

        btnDeleteRowCart.setText("Xoá dòng");
        btnDeleteRowCart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteRowCartActionPerformed(evt);
            }
        });

        txtCartSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCartSearchKeyReleased(evt);
            }
        });

        txtSearchProduct.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchProductKeyReleased(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel19.setText("Tổng tiền");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel20.setText("Giam gia");

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel22.setForeground(new java.awt.Color(204, 0, 51));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel22.setText("Thanh toan");

        jLabel6.setText("Địa chỉ:");

        lbAddress.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbAddress.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbAddress.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setText("Thanh toán");

        jButton1.setText("Kiểm tra mã giảm giá");

        jScrollPane2.setViewportView(jList1);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(24, 24, 24)
                            .addComponent(jLabel16)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtSearchProduct, javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                    .addComponent(cbbBrandProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(cbbCategoryProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(cbbSizeProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(cbbColorProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 1180, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel3)
                                .addComponent(jLabel2)
                                .addComponent(jLabel6)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addComponent(btnSelectCustomer)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lbAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1))
                                    .addGap(460, 460, 460)))
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addGap(11, 11, 11)
                                    .addComponent(jLabel1)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel19)
                                            .addComponent(jLabel18)
                                            .addComponent(jLabel24))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblTongTien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(btnPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(cbbBrandCart, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cbbColorCart, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cbbSizeCart, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(cbbCategoryCart, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(txtCartSearch))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnDeleteCart, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                        .addComponent(btnInsertCart, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnDeleteRowCart, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1181, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(8, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbColorProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSizeProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbCategoryProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbBrandProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(jLabel16)))
                .addGap(20, 20, 20)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbBrandCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbColorCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSizeCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbCategoryCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(txtCartSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btnDeleteCart)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDeleteRowCart)
                            .addComponent(btnInsertCart))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel3))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(lbAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel6))
                                        .addGap(155, 155, 155))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                        .addComponent(btnSelectCustomer)
                                        .addGap(150, 150, 150))))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel19)
                                    .addComponent(lblTongTien))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18)
                                    .addComponent(jLabel20))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel24)
                                    .addComponent(jLabel22))
                                .addGap(29, 29, 29)
                                .addComponent(btnPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addComponent(jLabel1)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1195, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 943, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 943, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblProductMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductMouseClicked
        if (evt.getClickCount() != 2) {
            return;
        }

        if (jTabbedPane1.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có giỏ hàng");
            return;
        }

        int row = ((JTable) evt.getSource()).getSelectedRow();
        if (row == -1) return;

        int tabIndex = jTabbedPane1.getSelectedIndex();
        if (tabIndex < 0 || tabIndex >= listInvoice.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giỏ hàng");
            return;
        }

        Invoice invoice = listInvoice.get(tabIndex);
        int invoiceId = invoice.getId();

        ProductVariant pv = listProductVariant.get(row);

        int productVariantId = pv.getId();
        float price = pv.getPrice();

        String input = JOptionPane.showInputDialog(
                this,
                "Nhập số lượng:",
                "Chọn số lượng",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input == null) return;

        int quantity;

        try {

            quantity = Integer.parseInt(input);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                return;
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ");
            return;
        }

        boolean result = cartService.addProductToCart(
                invoiceId,
                productVariantId,
                quantity,
                price
        );

        if (!result) return;

        initCart();
    }//GEN-LAST:event_tblProductMouseClicked

    private void btnInsertCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertCartActionPerformed
        Invoice invoice = new Invoice();

        invoice.setEmployeeId(employeeId);

        invoice.setTotalAmount(0);
        invoice.setStatus(OrderStatusEnum.DRAFT);

        boolean result = invoiceService.insert(invoice);

        if (!result) {
            JOptionPane.showMessageDialog(this, "Tạo giỏ hàng thất bại");
            return;
        }

        JOptionPane.showMessageDialog(this, "Tạo giỏ hàng thành công");

        initCart();

        if (jTabbedPane1.getTabCount() > 0) {
            jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);
        }
    }//GEN-LAST:event_btnInsertCartActionPerformed

    private void btnPaymentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPaymentActionPerformed

    }//GEN-LAST:event_btnPaymentActionPerformed

    private void btnSelectCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectCustomerActionPerformed
        CustomerUI panel = new CustomerUI(this, true);

        JDialog dialog = new JDialog();
        dialog.setTitle("Chọn khách hàng");
        dialog.setSize(1107, 784);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        dialog.add(panel);

        dialog.setVisible(true);
    }//GEN-LAST:event_btnSelectCustomerActionPerformed

    private void btnDeleteCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCartActionPerformed
        int index = jTabbedPane1.getSelectedIndex();

        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giỏ hàng cần xoá");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn xoá giỏ hàng này?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        Invoice invoice = listInvoice.get(index);

        boolean result = invoiceService.delete(invoice.getId());

        if (!result) {
            JOptionPane.showMessageDialog(this, "Chỉ xoá được giỏ hàng ở trạng thái nháp");
            return;
        }

        listInvoice.remove(index);
        jTabbedPane1.remove(index);

        JOptionPane.showMessageDialog(this, "Xoá giỏ hàng thành công");

        updateTotalAmount();
    }//GEN-LAST:event_btnDeleteCartActionPerformed

    private void btnDeleteRowCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRowCartActionPerformed
        JTable table = getCurrentCartTable();
        if (table == null) return;

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xoá");
            return;
        }

        int tabIndex = jTabbedPane1.getSelectedIndex();
        if(tabIndex < 0 || tabIndex >= listInvoice.size()) return;

        int invoiceId = listInvoice.get(tabIndex).getId();

        List<InvoiceItem> items = cartFilteredData.get(invoiceId);
        if(items == null || row >= items.size()) return;

        InvoiceItem item = items.get(row);

        boolean result = cartService.deleteRowCart(
            invoiceId,
            item.getProductVariantId()
        );

        if(result){
            initCart();
        }else{
            JOptionPane.showMessageDialog(this,"Xoá sản phẩm thất bại");
        }
    }//GEN-LAST:event_btnDeleteRowCartActionPerformed

    private void txtCartSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCartSearchKeyReleased
        filterCart();
    }//GEN-LAST:event_txtCartSearchKeyReleased

    private void txtSearchProductKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchProductKeyReleased
        initProduct();
    }//GEN-LAST:event_txtSearchProductKeyReleased

    private void tblCartMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCartMouseClicked
        JTable table = (JTable) evt.getSource();

        int row = table.getSelectedRow();
        if (row < 0) return;

        if (evt.getClickCount() != 2) return;

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        int productVariantId = (int) model.getValueAt(row, 9);
        Object currentQty = model.getValueAt(row, 6);

        String input = JOptionPane.showInputDialog(this, "Nhập số lượng:", currentQty);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int quantity = Integer.parseInt(input);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                return;
            }

            ProductVariant pv = null;
            for(ProductVariant p : listProductVariant){
                if(p.getId() == productVariantId){
                    pv = p;
                    break;
                }
            }

            if(pv == null){
                JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm");
                return;
            }

            int tabIndex = jTabbedPane1.getSelectedIndex();
            int invoiceId = listInvoice.get(tabIndex).getId();

            cartService.updateQuantity(invoiceId, productVariantId, quantity);

            initCart();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ");
        }
    }//GEN-LAST:event_tblCartMouseClicked

    /**
     * @param args the command line arguments
     */
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
        java.awt.EventQueue.invokeLater(() -> new OrderUI().setVisible(true));
    }
    
    // ================= IMAGE PREVIEW =================
    // Load ảnh + resize + cache
    private ImageIcon loadImage(String urlStr)  {

        if (urlStr == null || urlStr.isBlank()) return null;

        if (imageCache.containsKey(urlStr)) {
            return imageCache.get(urlStr);
        }

        try {

            ImageIcon imgIcon = new ImageIcon(new URL(urlStr));
            Image img = imgIcon.getImage();

            int targetW = 100;
            int targetH = 100;

            BufferedImage resized = new BufferedImage(
                    targetW,
                    targetH,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g2 = resized.createGraphics();

            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.drawImage(img, 0, 0, targetW, targetH, null);
            g2.dispose();

            ImageIcon icon = new ImageIcon(resized);
            icon.setDescription(urlStr); // lưu url ảnh gốc
            
            if(imageCache.size() > 200){
                imageCache.clear();
            }
            imageCache.put(urlStr, icon);

            return icon;

        } catch (Exception e) {
            return null;
        }
    }

    // Hover để xem ảnh lớn
    private void enableImagePreview(JTable table){

        ToolTipManager.sharedInstance().registerComponent(table);

        table.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e){

                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if(row < 0){
                    table.setToolTipText(null);
                    imagePreviewDialog.setVisible(false);
                    return;
                }

                if(col == 1 || col == 2){
                    Object value = table.getValueAt(row,col);
                    table.setToolTipText(value != null ? value.toString() : null);
                }

                if(col == 0){

                    Object iconObj = table.getValueAt(row,col);

                    if(iconObj instanceof ImageIcon icon){

                        Image img;

                        try{
                            String url = icon.getDescription();
                            img = new ImageIcon(new URL(url)).getImage();
                        }catch(Exception ex){
                            img = icon.getImage();
                        }

                        int dialogW = imagePreviewDialog.getWidth();
                        int dialogH = imagePreviewDialog.getHeight();

                        int imgW = img.getWidth(null);
                        int imgH = img.getHeight(null);

                        double scale = Math.min(
                                (double)dialogW/imgW,
                                (double)dialogH/imgH
                        );

                        Image scaled = img.getScaledInstance(
                                (int)(imgW * scale),
                                (int)(imgH * scale),
                                Image.SCALE_SMOOTH
                        );

                        imagePreviewLabel.setIcon(new ImageIcon(scaled));
                        imagePreviewDialog.setLocationRelativeTo(OrderUI.this);
                        imagePreviewDialog.setVisible(true);
                    }

                }else{
                    imagePreviewDialog.setVisible(false);
                }
            }
        });

        table.addMouseListener(new MouseAdapter(){

            @Override
            public void mouseExited(MouseEvent e){
                imagePreviewDialog.setVisible(false);
            }
        });
    }
    
    // ================= UTIL =================
    // Lấy id từ combobox
    private Integer getSelectedId(JComboBox combo){

        Object obj = combo.getSelectedItem();

        if(obj == null) return null;

        if(obj instanceof Size s) return s.getId();
        if(obj instanceof Color c) return c.getId();
        if(obj instanceof Brand b) return b.getId();
        if(obj instanceof Category c) return c.getId();

        return null;
    }
    
    private void centerTableHeader(JTable table) {

        //Header
        DefaultTableCellRenderer renderer =
                (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();

        renderer.setHorizontalAlignment(JLabel.CENTER);

        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD, 14f));
        
        //Body
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // Clear table
    private void clearTable(JTable table){
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
    }

    // Match search keyword
    private boolean matchSearch(String source, String keyword){

        if(keyword == null || keyword.isBlank()) return true;
        if(source == null) return false;

        return source.toLowerCase().contains(keyword.toLowerCase());
    }

    // Lấy table cart của tab hiện tại
    private JTable getCurrentCartTable(){

        int tabIndex = jTabbedPane1.getSelectedIndex();

        if(tabIndex == -1) return null;

        java.awt.Component comp = jTabbedPane1.getComponentAt(tabIndex);

        if(!(comp instanceof JPanel panel)) return null;

        java.awt.Component child = panel.getComponent(0);

        if(!(child instanceof JScrollPane scroll)) return null;

        return (JTable) scroll.getViewport().getView();
    }

    // Cập nhật tổng tiền
    private void updateTotalAmount(){

        int tabIndex = jTabbedPane1.getSelectedIndex();

        if(tabIndex < 0 || tabIndex >= listInvoice.size()){
            lblTongTien.setText("0");
            return;
        }

        int invoiceId = listInvoice.get(tabIndex).getId();

        List<InvoiceItem> items = cartData.get(invoiceId);

        float total = 0;

        if(items != null){
            for(InvoiceItem item : items){
                total += item.getQuantity() * item.getPrice();
            }
        }

        lblTongTien.setText(moneyFormat.format(total));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDeleteCart;
    private javax.swing.JButton btnDeleteRowCart;
    private javax.swing.JButton btnInsertCart;
    private javax.swing.JButton btnPayment;
    private javax.swing.JButton btnSelectCustomer;
    private javax.swing.JComboBox<String> cbbBrandCart;
    private javax.swing.JComboBox<String> cbbBrandProduct;
    private javax.swing.JComboBox<String> cbbCategoryCart;
    private javax.swing.JComboBox<String> cbbCategoryProduct;
    private javax.swing.JComboBox<String> cbbColorCart;
    private javax.swing.JComboBox<String> cbbColorProduct;
    private javax.swing.JComboBox<String> cbbSizeCart;
    private javax.swing.JComboBox<String> cbbSizeProduct;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbAddress;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JTable tblCart;
    private javax.swing.JTable tblProduct;
    private javax.swing.JTextField txtCartSearch;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
}