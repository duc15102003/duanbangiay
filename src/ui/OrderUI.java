package ui;

import entity.Brand;
import entity.Category;
import entity.Color;
import entity.Discount;
import entity.Employee;
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
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import service.DiscountService;
import service.EmployeeService;
import service.InvoiceService;
import service.ProductVariantService;
import service.SizeService;
import ui.auth.LoginUI;

// ===== iText =====
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import entity.Customer;

// ===== Swing =====
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

// ===== IO =====
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

// ===== Utils =====
import java.util.List;
import javax.swing.SwingUtilities;
import service.CustomerService;

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
    private DiscountService discountService = new DiscountService();
    private EmployeeService employeeService = new EmployeeService();
    private CustomerService customerService = new CustomerService();

    // ================= UI SUPPORT =================
    private JDialog imagePreviewDialog;
    private JLabel imagePreviewLabel;
    private Map<String, ImageIcon> imageCache = new HashMap<>();

    // ================= DATA =================
    private List<Invoice> listInvoice = new ArrayList<>();
    private List<ProductVariant> listProductVariant = new ArrayList<>();
    private Map<Integer, List<InvoiceItem>> cartData = new HashMap<>();
    private Map<Integer, List<InvoiceItem>> cartFilteredData = new HashMap<>();
    private Map<Integer, Discount> discountMap = new HashMap<>();
    private Map<Integer, Discount> selectedDiscountComboMap = new HashMap<>();
    
    //Thông tin id người mua được chọn và id người bán hàng
    private Map<Integer, Customer> cartCustomerMap = new HashMap<>();
    private Integer employeeId;

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
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
    }
    
    public OrderUI(int userId) {
        this.employeeId = userId;
        initComponents();
        setLocationRelativeTo(null);
        initData();
        setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initData() {
        initImagePreview();
        initFilterCombobox();
        initFilterEvents();

        initProduct();
        initCart();       

        txtDiscount.setVisible(false);
        btnApplyDiscount.setVisible(false);

        initTabListener();
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

        table.setRowHeight(50);
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

        if (employeeId == null) {
            JOptionPane.showMessageDialog(null, "Bạn chưa đăng nhập!");
            System.exit(0);
            return;
        }

        InvoiceFilter filter = new InvoiceFilter();
        filter.setStatus(OrderStatusEnum.PENDING_PAYMENT);

        listInvoice = cartService.findAll(filter, employeeId);

        String keyword = txtCartSearch.getText().trim();
        if (keyword.isBlank()) {
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
        loadDiscountCombo();
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
                    "Mã sản phẩm",
                    "Tên sản phẩm",
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
        table.getColumnModel().getColumn(10).setMinWidth(0);
        table.getColumnModel().getColumn(10).setMaxWidth(0);
        table.getColumnModel().getColumn(10).setWidth(0);
        
        table.setRowHeight(50);
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
            item.getProductCode(),
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
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // mã sp
        table.getColumnModel().getColumn(2).setPreferredWidth(275); // tên sp
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // danh mục
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // thương hiệu
        table.getColumnModel().getColumn(5).setPreferredWidth(90);  // màu
        table.getColumnModel().getColumn(6).setPreferredWidth(90);  // size
        table.getColumnModel().getColumn(7).setPreferredWidth(120); // số lượng
        table.getColumnModel().getColumn(8).setPreferredWidth(130); // đơn giá
        table.getColumnModel().getColumn(9).setPreferredWidth(130); // thành tiền
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
        table.getColumnModel().getColumn(7).setCellRenderer(right);
        // Number
        table.getColumnModel().getColumn(8).setCellRenderer(right);
        table.getColumnModel().getColumn(9).setCellRenderer(right);
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
        
    private void showQRDialog(int invoiceId) {

        JDialog dialog = new JDialog(this, "Thanh toán chuyển khoản", true);
        dialog.setSize(350, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Quét QR để thanh toán");
        lblTitle.setHorizontalAlignment(JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(lblTitle, BorderLayout.NORTH);

        JLabel lblQR = new JLabel();
        lblQR.setHorizontalAlignment(JLabel.CENTER);

        try {
            URL url = getClass().getResource("/common/images/qrcode.png");
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image img = icon.getImage().getScaledInstance(260, 260, Image.SCALE_SMOOTH);
                lblQR.setIcon(new ImageIcon(img));
            } else {
                lblQR.setText("Không tìm thấy QR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        panel.add(lblQR, BorderLayout.CENTER);

        float totalAmount = getFinalAmount(invoiceId);

        String invoiceCode = listInvoice
                .stream()
                .filter(i -> i.getId() == invoiceId)
                .findFirst()
                .map(Invoice::getCode)
                .orElse("N/A");

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel lblAmount = new JLabel("Số tiền: " + moneyFormat.format(totalAmount) + " VND");
        lblAmount.setFont(new Font("Arial", Font.BOLD, 15));
        lblAmount.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel lblContent = new JLabel("Nội dung: " + invoiceCode);
        lblContent.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        infoPanel.add(lblAmount);
        infoPanel.add(lblContent);

        panel.add(infoPanel, BorderLayout.SOUTH);
        dialog.add(panel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();

        JButton btnConfirm = new JButton("Xác nhận đã thanh toán");
        btnConfirm.setFont(new Font("Arial", Font.BOLD, 13));

        btnConfirm.addActionListener(e -> {

            boolean success = invoiceService.updateStatus(invoiceId, OrderStatusEnum.PAID);

            if (success) {

                int tabIndex = jTabbedPane1.getSelectedIndex();
                Customer customer = cartCustomerMap.get(tabIndex);

                String customerName = "";
                String customerPhone = "";
                String customerAddress = "";
                Integer selectedCustomerId = null;

                if (customer != null) {
                    selectedCustomerId = customer.getId();
                    customerName = customer.getName();
                    customerPhone = customer.getPhone();
                    customerAddress = customer.getAddress();
                }

                String employeeName = invoiceService.findNameById(employeeId);

                int discountAmount = 0;
                String text = lblGiamGia.getText();
                if (text != null && !text.isBlank()) {
                    String number = text.replaceAll("[^0-9]", "");
                    if (!number.isEmpty()) {
                        try {
                            discountAmount = Integer.parseInt(number);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                Discount appliedDiscount = discountMap.get(invoiceId);
                Integer discountId = (appliedDiscount != null) ? appliedDiscount.getId() : null;

                invoiceService.updatePaymentInfo(
                    invoiceId,
                    employeeId,
                    selectedCustomerId,
                    customerName,
                    customerPhone,
                    customerAddress,
                    employeeName,
                    discountAmount,
                    discountId
                );

                JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                dialog.dispose();
                showInvoiceDialog(invoiceId);

                discountMap.remove(invoiceId);
                selectedDiscountComboMap.remove(invoiceId);

                cartCustomerMap.remove(tabIndex);
                jLabel4.setText("");
                jLabel5.setText("");
                lbAddress.setText("");

                initCart();
                initProduct();

            } else {
                JOptionPane.showMessageDialog(this, "Thanh toán thất bại!");
            }
        });

        bottomPanel.add(btnConfirm);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private float getFinalAmount(int invoiceId) {

        List<InvoiceItem> items = cartData.get(invoiceId);

        float total = 0;

        if (items != null) {
            for (InvoiceItem item : items) {
                total += item.getQuantity() * item.getPrice();
            }
        }

        Discount discount = discountMap.get(invoiceId);

        float discountAmount = 0;

        if (discount != null) {

            String type = discount.getDiscountType() != null
                    ? discount.getDiscountType().trim()
                    : "";

            if ("%".equals(type)) {

                discountAmount = total * discount.getDiscountValue() / 100f;

                if (discount.getMaximumDiscount() != null) {
                    discountAmount = Math.min(discountAmount, discount.getMaximumDiscount());
                }

            } else if ("Tiền mặt".equalsIgnoreCase(type)) {
                discountAmount = discount.getDiscountValue();
            }

            if (discountAmount > total) {
                discountAmount = total;
            }
        }

        return total - discountAmount;
    }
    
    public void setSelectedCustomer(int customerId, String name, String phone, String address) {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName(name);
        customer.setPhone(phone);
        customer.setAddress(address);

        int tabIndex = jTabbedPane1.getSelectedIndex();
        cartCustomerMap.put(tabIndex, customer);

        jLabel4.setText(name);
        jLabel5.setText(phone);
        lbAddress.setText(address);
    }
    
    
        private void initTabListener() {
        jTabbedPane1.addChangeListener(e -> {
            int tabIndex = jTabbedPane1.getSelectedIndex();
            Customer c = cartCustomerMap.get(tabIndex);

            if (c != null) {
                jLabel4.setText(c.getName());
                jLabel5.setText(c.getPhone());
                lbAddress.setText(c.getAddress());
            } else {
                jLabel4.setText("");
                jLabel5.setText("");
                lbAddress.setText("");
            }

            updateTotalAmount();
            loadDiscountCombo();
        });

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                if (listInvoice != null && !listInvoice.isEmpty()) {
                    loadDiscountCombo();
                }
            }
        });
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
        lblGiamGia = new javax.swing.JLabel();
        lblThanhToan = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lbAddress = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        btnApplyDiscount = new javax.swing.JButton();
        txtDiscount = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        cbbDiscount = new javax.swing.JComboBox<>();

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
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
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

        lblGiamGia.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblGiamGia.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblGiamGia.setText("Giam gia");

        lblThanhToan.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblThanhToan.setForeground(new java.awt.Color(204, 0, 51));
        lblThanhToan.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblThanhToan.setText("Thanh toan");

        jLabel6.setText("Địa chỉ:");

        lbAddress.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lbAddress.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbAddress.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setText("Thanh toán");

        btnApplyDiscount.setText("Áp dụng mã giảm giá");
        btnApplyDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnApplyDiscountActionPerformed(evt);
            }
        });

        jLabel7.setForeground(new java.awt.Color(204, 0, 51));
        jLabel7.setText("Đăng xuất");
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jPanel5Layout.createSequentialGroup()
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
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnSelectCustomer)
                                    .addComponent(jLabel6))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lbAddress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                                .addGap(33, 33, 33))
                                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(btnApplyDiscount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(23, 23, 23))))
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(jLabel7))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(128, 128, 128)
                                .addComponent(cbbDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(36, 36, 36)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jLabel1))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel5Layout.createSequentialGroup()
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel19)
                                        .addComponent(jLabel18)
                                        .addComponent(jLabel24))
                                    .addGap(18, 18, 18)
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(lblTongTien, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblGiamGia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblThanhToan, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(btnPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
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
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1181, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbColorProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSizeProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbCategoryProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbBrandProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel16)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbbBrandCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbColorCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbSizeCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbbCategoryCart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addComponent(txtCartSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btnDeleteCart)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDeleteRowCart)
                            .addComponent(btnInsertCart))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(0, 0, 0)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel19)
                                        .addComponent(lblTongTien))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGap(2, 2, 2)
                                        .addComponent(cbbDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel18)
                                    .addComponent(lblGiamGia))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel24)
                                    .addComponent(lblThanhToan))
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGap(15, 15, 15)
                                        .addComponent(btnSelectCustomer))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnPayment, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addComponent(txtDiscount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(btnApplyDiscount))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addGap(3, 3, 3)
                        .addComponent(jLabel3)
                        .addGap(27, 27, 27)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(lbAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20))
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
            .addGap(0, 687, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tblProductMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductMouseClicked
        if (evt.getClickCount() != 2) return;

        if (jTabbedPane1.getTabCount() == 0) {
            JOptionPane.showMessageDialog(this, "Chưa có giỏ hàng");
            return;
        }

        int row = tblProduct.getSelectedRow();
        if (row == -1) return;

        int tabIndex = jTabbedPane1.getSelectedIndex();
        if (tabIndex < 0 || tabIndex >= listInvoice.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giỏ hàng");
            return;
        }

        int invoiceId = listInvoice.get(tabIndex).getId();

        ProductVariant pv = listProductVariant.get(row);

        String input = JOptionPane.showInputDialog(this, "Nhập số lượng:");

        if (input == null) return;

        try {
            int quantity = Integer.parseInt(input);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                return;
            }

            boolean result = cartService.addProductToCart(
                    invoiceId,
                    pv.getId(),
                    quantity,
                    pv.getPrice()
            );

            if (!result) {
                JOptionPane.showMessageDialog(this, "Không đủ hàng!");
                return;
            }

            initCart();
            initProduct();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ");
        }
    }//GEN-LAST:event_tblProductMouseClicked

    private void btnInsertCartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInsertCartActionPerformed
        Invoice invoice = new Invoice();

        invoice.setEmployeeId(employeeId);

        invoice.setTotalAmount(0);
        invoice.setStatus(OrderStatusEnum.PENDING_PAYMENT);

        boolean result = invoiceService.insert(invoice);
        
        if (employeeId <= 0) {
            JOptionPane.showMessageDialog(this, "Bạn chưa đăng nhập");
            return;
        }

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
        int tabIndex = jTabbedPane1.getSelectedIndex();

        if (tabIndex < 0 || tabIndex >= listInvoice.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giỏ hàng!");
            return;
        }

        int invoiceId = listInvoice.get(tabIndex).getId();

        // ===== CHECK GIỎ HÀNG =====
        JTable table = getCurrentCartTable();
        if (table == null || table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống!");
            return;
        }

        // ===== LẤY FINAL AMOUNT =====
        float finalAmount = getFinalAmount(invoiceId);

        if (!invoiceService.updateTotalAmount(invoiceId, finalAmount)) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật tổng tiền!");
            return;
        }

        // ===== LẤY THÔNG TIN KHÁCH & NHÂN VIÊN =====
        Customer customer = cartCustomerMap.get(tabIndex);

        if (customer == null) {
            customer = customerService.findById(1);
            cartCustomerMap.put(tabIndex, customer); 
        }
        
        String customerName = (customer != null) ? customer.getName() : "Khách lẻ";
        String customerPhone = (customer != null) ? customer.getPhone() : "";
        String customerAddress = (customer != null) ? customer.getAddress() : "";

        String employeeName = invoiceService.findNameById(employeeId);

        // ===== CHỌN PHƯƠNG THỨC =====
        Object[] options = {"Tiền mặt", "Chuyển khoản"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Chọn phương thức thanh toán",
                "Thanh toán",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1) return;

        if (choice == 0) { // TIỀN MẶT
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận thanh toán tiền mặt?\nSố tiền: " + moneyFormat.format(finalAmount) + " VND",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                if (invoiceService.updateStatus(invoiceId, OrderStatusEnum.PAID)) {

                    invoiceService.updatePaymentType(invoiceId, "Tiền mặt");

                    // TRỪ SỐ LƯỢNG PHIẾU GIẢM GIÁ
                    Discount appliedDiscount = discountMap.get(invoiceId);
                    if (appliedDiscount != null) {
                        discountService.useDiscount(appliedDiscount.getCode());
                    }

                    int discountAmount = 0;
                    String text = lblGiamGia.getText();
                    if (text != null && !text.isBlank()) {
                        try {
                            discountAmount = Integer.parseInt(text.replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                            discountAmount = 0;
                        }
                    }

                    Integer discountId = (appliedDiscount != null) ? appliedDiscount.getId() : null;

                    invoiceService.updatePaymentInfo(
                            invoiceId,
                            employeeId,
                            (customer != null ? customer.getId() : null),
                            customerName,
                            customerPhone,
                            customerAddress,
                            employeeName,
                            discountAmount,
                            discountId
                    );

                    JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                    showInvoiceDialog(invoiceId);

                    // Xóa discount của tab sau khi thanh toán
                    discountMap.remove(invoiceId);
                    selectedDiscountComboMap.remove(invoiceId);

                    initCart();
                    initProduct();
                    txtDiscount.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Thanh toán thất bại!");
                }
            }

        } else if (choice == 1) { // CHUYỂN KHOẢN
            invoiceService.updatePaymentType(invoiceId, "Chuyển khoản");
            showQRDialog(invoiceId);

            Discount appliedDiscount = discountMap.get(invoiceId);
            if (appliedDiscount != null) {
                discountService.useDiscount(appliedDiscount.getCode());
                discountMap.remove(invoiceId);
                selectedDiscountComboMap.remove(invoiceId);
            }

            txtDiscount.setText("");
        }
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
    int invoiceId = invoice.getId();

    // Hoàn stock tất cả sản phẩm trong giỏ
    List<InvoiceItem> items = cartService.findByInvoiceId(invoiceId, null);
    if (items != null) {
        for (InvoiceItem item : items) {
            boolean ok = productVariantService.increaseStock(item.getProductVariantId(), item.getQuantity());
            if (!ok) {
                System.err.println("Không thể hoàn stock cho productVariantId=" + item.getProductVariantId());
            }
        }
    }

    // Xoá giỏ
    boolean result = invoiceService.delete(invoiceId);
    if (!result) {
        JOptionPane.showMessageDialog(this, "Chỉ xoá được giỏ hàng ở trạng thái nháp");
        return;
    }

    listInvoice.remove(index);
    jTabbedPane1.remove(index);

    JOptionPane.showMessageDialog(this, "Xoá giỏ hàng thành công");

    // Refresh
    initCart();
    initProduct();
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
        int invoiceId = listInvoice.get(tabIndex).getId();

        DefaultTableModel model = (DefaultTableModel) table.getModel();

        // ===== PV_ID =====
        Object pvObj = model.getValueAt(row, 10);
        int productVariantId = Integer.parseInt(pvObj.toString());

        Object qtyObj = model.getValueAt(row, 7);
        int quantity = Integer.parseInt(qtyObj.toString());

        boolean result = cartService.deleteRowCart(
                invoiceId,
                productVariantId,
                quantity
        );

        if (result) {
            initCart();
            initProduct();
        } else {
            JOptionPane.showMessageDialog(this, "Xoá sản phẩm thất bại");
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

        int productVariantId = (int) model.getValueAt(row, 10);
        int currentQty = Integer.parseInt(model.getValueAt(row, 7).toString());

        String input = JOptionPane.showInputDialog(this, "Nhập số lượng:", currentQty);

        if (input == null || input.trim().isEmpty()) return;

        try {
            int newQty = Integer.parseInt(input);

            if (newQty <= 0) {
                JOptionPane.showMessageDialog(this, "Số lượng phải > 0");
                return;
            }

            int tabIndex = jTabbedPane1.getSelectedIndex();
            int invoiceId = listInvoice.get(tabIndex).getId();

            boolean result = cartService.updateQuantity(
                    invoiceId,
                    productVariantId,
                    newQty
            );

            if (!result) {
                JOptionPane.showMessageDialog(this, "Không đủ hàng!");
                return;
            }

            initCart();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ");
        }
    }//GEN-LAST:event_tblCartMouseClicked

    private void btnApplyDiscountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnApplyDiscountActionPerformed
        String code = txtDiscount.getText();

        if (code == null || code.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã giảm giá!");
            return;
        }

        int tabIndex = jTabbedPane1.getSelectedIndex();

        if (tabIndex < 0 || tabIndex >= listInvoice.size()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giỏ hàng!");
            return;
        }

        int invoiceId = listInvoice.get(tabIndex).getId();

        float total = getTotalFromCart();

        // Lấy customer của tab hiện tại
        Customer customer = cartCustomerMap.get(tabIndex);
        Integer customerId = (customer != null) ? customer.getId() : null;

        Discount discount = discountService.checkDiscount(code, invoiceId, total, customerId);

        if (discount == null) {
            JOptionPane.showMessageDialog(this, "Mã giảm giá không hợp lệ!");
            return;
        }

        discountMap.put(invoiceId, discount);

        JOptionPane.showMessageDialog(this, "Áp dụng thành công!");

        txtDiscount.setText("");

        updateTotalAmount();
    }//GEN-LAST:event_btnApplyDiscountActionPerformed

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {

            // Mở Login
            LoginUI login = new LoginUI();
            login.setLocationRelativeTo(null);
            login.setVisible(true);

            // Đóng tất cả window đang mở
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window != login) {
                    window.dispose();
                }
            }
        }
    }//GEN-LAST:event_jLabel7MouseClicked

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
            lblGiamGia.setText("0");
            lblThanhToan.setText("0");
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

        Discount currentDiscount = discountMap.get(invoiceId);

        float discountAmount = 0;

        if (currentDiscount != null) {

            String type = currentDiscount.getDiscountType() != null 
                    ? currentDiscount.getDiscountType().trim() 
                    : "";

            if ("%".equals(type)) {

                discountAmount = total * currentDiscount.getDiscountValue() / 100f;

                if (currentDiscount.getMaximumDiscount() != null) {
                    discountAmount = Math.min(discountAmount, currentDiscount.getMaximumDiscount());
                }

            } else if ("Tiền mặt".equalsIgnoreCase(type)) {

                discountAmount = currentDiscount.getDiscountValue();
            }

            if (discountAmount > total) {
                discountAmount = total;
            }
        }

        float finalAmount = total - discountAmount;

        lblTongTien.setText(moneyFormat.format(total));
        lblGiamGia.setText(discountAmount > 0 
                ? "- " + moneyFormat.format(discountAmount) 
                : "0");
        lblThanhToan.setText(moneyFormat.format(finalAmount));

        txtDiscount.setText(currentDiscount != null ? currentDiscount.getCode() : "");
    }
    
    private float getTotalFromCart() {

        int tabIndex = jTabbedPane1.getSelectedIndex();

        if (tabIndex < 0 || tabIndex >= listInvoice.size()) {
            return 0;
        }

        int invoiceId = listInvoice.get(tabIndex).getId();

        List<InvoiceItem> items = cartData.get(invoiceId);

        float total = 0;

        if (items != null) {
            for (InvoiceItem item : items) {
                total += item.getQuantity() * item.getPrice();
            }
        }

        return total;
    }
    
    private float parseMoney(String text) {
        if (text == null || text.isBlank()) return 0;

        String number = text.replaceAll("[^0-9]", "");

        if (number.isEmpty()) return 0;

        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void showInvoiceDialog(int invoiceId) {

        JDialog dialog = new JDialog(this, "Hóa đơn", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // ===== LẤY DATA =====
        List<InvoiceItem> items = cartService.findByInvoiceId(invoiceId, null);
        float total = parseMoney(lblTongTien.getText());
        float discountAmount = parseMoney(lblGiamGia.getText());
        float finalAmount = parseMoney(lblThanhToan.getText());

        // ===== INVOICE =====
        Invoice invoice = invoiceService.findById(invoiceId);

        String invoiceCode = (invoice != null) ? invoice.getCode() : "N/A";
        String paymentType = (invoice != null && invoice.getPaymentType() != null)
                                                ? invoice.getPaymentType()
                                                : "N/A";

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String createdAt = (invoice != null && invoice.getCreatedAt() != null)
                ? invoice.getCreatedAt().format(formatter)
                : "N/A";

        // ===== EMPLOYEE =====
        Employee emp = employeeService.findById(employeeId);

        String employeeName = (emp != null && emp.getName() != null) ? emp.getName() : "N/A";
        String employeeCode = (emp != null && emp.getCode() != null) ? emp.getCode() : "N/A";

        // ===== CUSTOMER =====
        int tabIndex = jTabbedPane1.getSelectedIndex();
        Customer customer = cartCustomerMap.get(tabIndex);

        String customerName = (customer != null) ? customer.getName() : "Khách lẻ";
        String customerPhone = (customer != null) ? customer.getPhone() : "";
        String customerAddress = (customer != null) ? customer.getAddress() : "";

        // ===== HEADER =====
        JLabel lblTitle = centerLabel("HÓA ĐƠN THANH TOÁN");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        panel.add(lblTitle);
        panel.add(centerLabel("------------------------------"));

        // ===== INVOICE INFO =====
        panel.add(centerLabel("Mã HĐ: " + invoiceCode));
        panel.add(centerLabel("Ngày: " + createdAt));
        panel.add(centerLabel("Thanh toán: " + paymentType));

        panel.add(centerLabel("------------------------------"));

        // ===== SELLER =====
        panel.add(centerLabel("Nhân viên: " + employeeCode + " - " + employeeName));

        panel.add(centerLabel("------------------------------"));

        // ===== CUSTOMER =====
        panel.add(centerLabel("Khách: " + customerName));
        panel.add(centerLabel("SĐT: " + customerPhone));
        panel.add(centerLabel("Địa chỉ: " + customerAddress));

        panel.add(centerLabel("------------------------------"));

        // ===== ITEMS =====
        for (InvoiceItem item : items) {
            String line = item.getProductName()
                    + " | SL: " + item.getQuantity()
                    + " | " + moneyFormat.format(item.getPrice());

            panel.add(centerLabel(line));
        }

        panel.add(centerLabel("------------------------------"));

        // ===== TOTAL =====
        panel.add(centerLabel("Tổng: " + moneyFormat.format(total)));
        panel.add(centerLabel("Giảm giá: - " + moneyFormat.format(discountAmount)));
        panel.add(centerLabel("Thanh toán: " + moneyFormat.format(finalAmount)));

        // ===== SCROLL =====
        JScrollPane scrollPane = new JScrollPane(panel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // ===== BUTTON =====
        JButton btnExport = new JButton("Xuất PDF");
        btnExport.addActionListener(e -> exportInvoiceToPDF(invoiceId));

        JButton btnClose = new JButton("Đóng");
        btnClose.addActionListener(e -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.add(btnExport);
        bottom.add(btnClose);

        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    private JLabel centerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setHorizontalAlignment(JLabel.CENTER);
        return label;
    }
    
    private void loadDiscountCombo() {
        JComboBox<Object> combo = (JComboBox<Object>) (JComboBox<?>) cbbDiscount;

        for (java.awt.event.ActionListener al : combo.getActionListeners()) {
            combo.removeActionListener(al);
        }

        combo.removeAllItems();
        combo.addItem(null);

        float total = getTotalFromCart();
        List<Discount> validDiscounts = discountService.getValidDiscounts(total);

        for (Discount d : validDiscounts) {
            combo.addItem(d);
        }

        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("-- Chọn mã giảm giá --");
                } else if (value instanceof Discount d) {
                    String label = d.getCode() + " | ";
                    if ("%".equals(d.getDiscountType())) {
                        label += d.getDiscountValue() + "%";
                        if (d.getMaximumDiscount() != null) {
                            label += " (tối đa " + moneyFormat.format(d.getMaximumDiscount()) + ")";
                        }
                    } else {
                        label += moneyFormat.format(d.getDiscountValue()) + " VND";
                    }
                    setText(label);
                }
                return this;
            }
        });

        int tabIndex = jTabbedPane1.getSelectedIndex();
        if (tabIndex >= 0 && tabIndex < listInvoice.size()) {
            int invoiceId = listInvoice.get(tabIndex).getId();
            Discount saved = selectedDiscountComboMap.get(invoiceId);
            if (saved != null) {
                for (int i = 0; i < combo.getItemCount(); i++) {
                    Object item = combo.getItemAt(i);
                    if (item instanceof Discount d && d.getId().equals(saved.getId())) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        combo.addActionListener(e -> {
            Object selected = combo.getSelectedItem();
            if (!(selected instanceof Discount discount)) return;
            int tab = jTabbedPane1.getSelectedIndex();
            if (tab < 0 || tab >= listInvoice.size()) return;
            int invoiceId = listInvoice.get(tab).getId();

            selectedDiscountComboMap.put(invoiceId, discount);
            discountMap.put(invoiceId, discount);

            JOptionPane.showMessageDialog(this, "Áp dụng mã \"" + discount.getCode() + "\" thành công!");
            updateTotalAmount();
        });
    }
    
    //Xuất PDF hoá đơn
    private void exportInvoiceToPDF(int invoiceId) {
    try {
        // ===== LẤY DATA =====
        List<InvoiceItem> items = cartService.findByInvoiceId(invoiceId, null);
        float total = invoiceService.getTotalAmount(invoiceId);

        float discountAmount = 0;
        String text = lblGiamGia.getText();

        if (text != null && !text.isBlank()) {
            String number = text.replaceAll("[^0-9]", "");
            if (!number.isEmpty()) {
                try {
                    discountAmount = Float.parseFloat(number);
                } catch (NumberFormatException ignored) {}
            }
        }

        float finalAmount = total - discountAmount;

        Invoice invoice = invoiceService.findById(invoiceId);
        String invoiceCode = (invoice != null) ? invoice.getCode() : "N/A";
        String paymentType = (invoice != null && invoice.getPaymentType() != null)
                ? invoice.getPaymentType() : "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String createdAt = (invoice != null && invoice.getCreatedAt() != null)
                ? invoice.getCreatedAt().format(formatter) : "N/A";

        Employee emp = employeeService.findById(employeeId);
        String employeeName = (emp != null) ? emp.getName() : "N/A";
        String employeeCode = (emp != null) ? emp.getCode() : "N/A";

        // ===== CUSTOMER =====
        int tabIndex = jTabbedPane1.getSelectedIndex();
        Customer customer = cartCustomerMap.get(tabIndex);

        String customerName = (customer != null) ? customer.getName() : "Khách lẻ";
        String customerPhone = (customer != null) ? customer.getPhone() : "";
        String customerAddress = (customer != null) ? customer.getAddress() : "";

        // ===== CHỌN FILE =====
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("invoice_" + invoiceCode + ".pdf"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        // ===== FONT =====
        InputStream is = getClass().getResourceAsStream("/common/fonts/Roboto-Regular.ttf");
        if (is == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy font!");
            return;
        }

        byte[] fontBytes = is.readAllBytes();
        BaseFont bf = BaseFont.createFont(
                "Roboto-Regular.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                true,
                fontBytes,
                null
        );

        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(bf, 18, com.itextpdf.text.Font.BOLD);
        com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(bf, 12);
        com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(bf, 12, com.itextpdf.text.Font.BOLD);

        // ===== PDF =====
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // ===== HEADER =====
        Paragraph title = new Paragraph("HÓA ĐƠN THANH TOÁN", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // ===== INFO =====
        document.add(new Paragraph("Mã HĐ: " + invoiceCode, normalFont));
        document.add(new Paragraph("Ngày: " + createdAt, normalFont));
        document.add(new Paragraph("Thanh toán: " + paymentType, normalFont));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Nhân viên: " + employeeCode + " - " + employeeName, normalFont));

        document.add(new Paragraph(" "));

        document.add(new Paragraph("Khách: " + customerName, normalFont));
        document.add(new Paragraph("SĐT: " + customerPhone, normalFont));
        document.add(new Paragraph("Địa chỉ: " + customerAddress, normalFont));

        document.add(new Paragraph(" "));

        // ===== TABLE =====
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        float[] widths = {4f, 1.5f, 2f, 2f};
        table.setWidths(widths);

        addHeader(table, "Sản phẩm", boldFont);
        addHeader(table, "SL", boldFont);
        addHeader(table, "Đơn giá", boldFont);
        addHeader(table, "Thành tiền", boldFont);

        for (InvoiceItem item : items) {
            float lineTotal = item.getPrice() * item.getQuantity();

            addCell(table, item.getProductName(), normalFont);
            addCellCenter(table, String.valueOf(item.getQuantity()), normalFont);
            addCell(table, moneyFormat.format(item.getPrice()), normalFont);
            addCell(table, moneyFormat.format(lineTotal), normalFont);
        }

        document.add(table);

        // ===== TOTAL =====
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingBefore(10f);

        addCell(totalTable, "Tổng:", boldFont);
        addCell(totalTable, moneyFormat.format(total), normalFont);

        addCell(totalTable, "Giảm giá:", boldFont);
        addCell(totalTable, "- " + moneyFormat.format(discountAmount), normalFont);

        addCell(totalTable, "Thanh toán:", boldFont);
        addCell(totalTable, moneyFormat.format(finalAmount), boldFont);

        document.add(totalTable);

        document.close();

        JOptionPane.showMessageDialog(this, "Xuất PDF thành công!");

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi xuất PDF!");
    }
}

    private void addHeader(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        table.addCell(cell);
    }
    
    private void addCellCenter(PdfPTable table, String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }
    
    private void addLine(Document document, com.itextpdf.text.Font font) throws Exception {
        document.add(new Paragraph("----------------------------------", font));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnApplyDiscount;
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
    private javax.swing.JComboBox<String> cbbDiscount;
    private javax.swing.JComboBox<String> cbbSizeCart;
    private javax.swing.JComboBox<String> cbbSizeProduct;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lbAddress;
    private javax.swing.JLabel lblGiamGia;
    private javax.swing.JLabel lblThanhToan;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JTable tblCart;
    private javax.swing.JTable tblProduct;
    private javax.swing.JTextField txtCartSearch;
    private javax.swing.JTextField txtDiscount;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
}