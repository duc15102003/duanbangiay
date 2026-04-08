package ui;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;

import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import dao.CartDAO;
import dao.InvoiceDAO;
import entity.Employee;

import entity.Invoice;
import entity.InvoiceItem;
import entity.filter.InvoiceFilter;

import java.awt.Image;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.text.DecimalFormat;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import service.CartService;
import service.CustomerService;
import service.EmployeeService;
import service.InvoiceService;

public class InvoiceUI extends javax.swing.JPanel {
    
    private final CartDAO cartDAO = new CartDAO();
    private final CustomerService customerService = new CustomerService();
    private final CartService cartService = new CartService();
    private final EmployeeService employeeService = new EmployeeService();
    private final InvoiceService invoiceService = new InvoiceService();

    private List<Invoice> invoices = new ArrayList<>();
    private List<InvoiceItem> currentItems = new ArrayList<>();

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");
    
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private JDialog imagePreviewDialog;
    private JLabel imagePreviewLabel;

    private Map<String, ImageIcon> imageCache = new HashMap<>();

    private final ExecutorService imageLoader = Executors.newFixedThreadPool(4);

    public InvoiceUI() {
        initComponents();

        initTable();
        
        initFilterComboBox();

        initImagePreview();
        enableTableImageHover();

        centerTable(tblInvoice); 
        centerTable(tblInvoiceItem, 0);

        loadInvoiceTable();
        
        dcFrom.setDateFormatString("dd-MM-yyyy");
        dcTo.setDateFormatString("dd-MM-yyyy");
        
        dcFrom.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                loadInvoiceTable();
            }
        });

        dcTo.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                loadInvoiceTable();
            }
        });
        
        dcFrom.setDateFormatString("dd-MM-yyyy");
        dcTo.setDateFormatString("dd-MM-yyyy");

        javax.swing.JTextField fromEditor =
            (javax.swing.JTextField) dcFrom.getDateEditor().getUiComponent();

        javax.swing.JTextField toEditor =
            (javax.swing.JTextField) dcTo.getDateEditor().getUiComponent();

        fromEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){ loadInvoiceTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ loadInvoiceTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ loadInvoiceTable(); }
        });

        toEditor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            public void insertUpdate(javax.swing.event.DocumentEvent e){ loadInvoiceTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ loadInvoiceTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ loadInvoiceTable(); }
        });
        
        cbbBrand.addActionListener(e -> reloadInvoiceItems());
        cbbCategory.addActionListener(e -> reloadInvoiceItems());
        cbbSize.addActionListener(e -> reloadInvoiceItems());
        cbbColor.addActionListener(e -> reloadInvoiceItems());
        initStatusComboBox();
    }

    public void resetForm(){

        dcFrom.setDate(null);
        dcTo.setDate(null);

        txtSearchInvoice.setText("");
        txtSearchInvoiceItem.setText("");

        loadInvoiceTable();

        DefaultTableModel model =
                (DefaultTableModel) tblInvoiceItem.getModel();
        model.setRowCount(0);
    }

    private void loadInvoiceTable() {

        InvoiceFilter filter = new InvoiceFilter();

        if (dcFrom.getDate() != null) {

            filter.setFromCreatedDate(
                dcFrom.getDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atStartOfDay()
            );
        }

        if (dcTo.getDate() != null) {

            filter.setToCreatedDate(
                dcTo.getDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(23, 59, 59)
            );
        }

        filter.setSearch(txtSearchInvoice.getText().trim());

        List<Invoice> list = cartDAO.findAll(filter, null);

        String selectedStatus = (String) cbbStatus.getSelectedItem();

        invoices = list.stream()
            //.filter(i -> i.getStatus() != enums.OrderStatusEnum.PENDING_PAYMENT)

            // ===== FILTER STATUS =====
            .filter(i -> {
                if (selectedStatus == null || selectedStatus.equals("Tất cả")) {
                    return true;
                }
                return i.getStatus().getLabel().equals(selectedStatus);
            })
            .toList();

        DefaultTableModel model = (DefaultTableModel) tblInvoice.getModel();
        model.setRowCount(0);

        for (Invoice i : invoices) {

            String createdDate = "";

            if (i.getCreatedAt() != null) {
                createdDate = i.getCreatedAt().format(dateFormat);
            }

            double totalBeforeDiscount = i.getTotalAmount() + i.getDiscountAmount();

            model.addRow(new Object[]{
                i.getCode(),
                i.getEmployeeName(),
                i.getCustomerName(),
                i.getCustomerPhone(),
                i.getCustomerAddress(),
                moneyFormat.format(totalBeforeDiscount),  
                moneyFormat.format(i.getDiscountAmount()),
                moneyFormat.format(i.getTotalAmount()),
                createdDate,
                i.getPaymentType(),
                i.getStatus().getLabel()
            });
        }

        ((DefaultTableModel) tblInvoiceItem.getModel()).setRowCount(0);
    }

    private void loadInvoiceItem(int invoiceId){

        List<InvoiceItem> list =
            cartDAO.findByInvoiceId(
                invoiceId,
                txtSearchInvoiceItem.getText().trim()
            );

        String brand = (String) cbbBrand.getSelectedItem();
        String category = (String) cbbCategory.getSelectedItem();
        String size = (String) cbbSize.getSelectedItem();
        String color = (String) cbbColor.getSelectedItem();

        currentItems = list.stream()
            .filter(i -> brand == null || brand.equals("Tất cả") || brand.equals(i.getBrandName()))
            .filter(i -> category == null || category.equals("Tất cả") || category.equals(i.getCategoryName()))
            .filter(i -> size == null || size.equals("Tất cả") || size.equals(i.getSizeName()))
            .filter(i -> color == null || color.equals("Tất cả") || color.equals(i.getColorName()))
            .toList();

        DefaultTableModel model =
                (DefaultTableModel) tblInvoiceItem.getModel();

        model.setRowCount(0);

        for(int i = 0; i < currentItems.size(); i++){

            InvoiceItem item = currentItems.get(i);
            
            double totalPrice = item.getQuantity() * item.getPrice();

            model.addRow(new Object[]{
                null,                      
                item.getProductCode(),   
                item.getProductName(),
                item.getBrandName(),
                item.getCategoryName(),
                item.getColorName(),
                item.getSizeName(),
                item.getQuantity(),
                moneyFormat.format(item.getPrice()),
                moneyFormat.format(totalPrice) 
            });

            loadImage(item.getImage(), i);
        }
    }
    
    private void reloadInvoiceItems(){

        int row = tblInvoice.getSelectedRow();

        if(row < 0) return;

        int modelRow = tblInvoice.convertRowIndexToModel(row);

        Invoice invoice = invoices.get(modelRow);

        loadInvoiceItem(invoice.getId());
    }
    
    private void initFilterComboBox(){

            cbbBrand.removeAllItems();
            cbbCategory.removeAllItems();
            cbbSize.removeAllItems();
            cbbColor.removeAllItems();

            cbbBrand.addItem("Tất cả");
            cbbCategory.addItem("Tất cả");
            cbbSize.addItem("Tất cả");
            cbbColor.addItem("Tất cả");
        }
    
    private void initStatusComboBox() {

        cbbStatus.removeAllItems();

        cbbStatus.addItem("Tất cả");

        for (enums.OrderStatusEnum status : enums.OrderStatusEnum.values()) {
            cbbStatus.addItem(status.getLabel());
        }

        // ===== EVENT FILTER =====
        cbbStatus.addActionListener(e -> loadInvoiceTable());
    }

        public void loadFilterData(
            java.util.List<entity.Brand> brands,
            java.util.List<entity.Category> categories,
            java.util.List<entity.Size> sizes,
            java.util.List<entity.Color> colors){

        cbbBrand.removeAllItems();
        cbbCategory.removeAllItems();
        cbbSize.removeAllItems();
        cbbColor.removeAllItems();

        cbbBrand.addItem("Tất cả");
        for(entity.Brand b : brands){
            cbbBrand.addItem(b.getName());
        }

        cbbCategory.addItem("Tất cả");
        for(entity.Category c : categories){
            cbbCategory.addItem(c.getName());
        }

        cbbSize.addItem("Tất cả");
        for(entity.Size s : sizes){
            cbbSize.addItem(s.getName());
        }

        cbbColor.addItem("Tất cả");
        for(entity.Color c : colors){
            cbbColor.addItem(c.getName());
        }
    }
    
    private void loadImage(String url, int row){

        if(url == null || url.isBlank()) return;

        ImageIcon cached = imageCache.get(url);

        if(cached != null){
            tblInvoiceItem.setValueAt(cached,row,0);
            return;
        }

        imageLoader.submit(() -> {

            try{

                ImageIcon icon = new ImageIcon(new java.net.URL(url));

                Image scaled = scaleImage(icon.getImage(),90,90);

                ImageIcon finalIcon = new ImageIcon(scaled);

                imageCache.put(url, finalIcon);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    if(row < tblInvoiceItem.getRowCount()){
                        tblInvoiceItem.setValueAt(finalIcon,row,0);
                    }
                });

            }catch(Exception e){
            }

        });
    }

    private void centerTable(JTable table){

        DefaultTableCellRenderer center =
                new DefaultTableCellRenderer();

        center.setHorizontalAlignment(SwingConstants.CENTER);

        for(int i = 0; i < table.getColumnCount(); i++){
            table.getColumnModel()
                 .getColumn(i)
                 .setCellRenderer(center);
        }

        DefaultTableCellRenderer header =
                (DefaultTableCellRenderer)
                table.getTableHeader().getDefaultRenderer();

        header.setHorizontalAlignment(SwingConstants.CENTER);
    }
    
    private void initTable(){

        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Ảnh", "Mã sản phẩm", "Tên sản phẩm", "Thương hiệu",
                "Danh mục", "Màu sắc", "Kích thước",
                "Số lượng", "Đơn giá", "Thành tiền" 
            }
        ){
            @Override
            public Class<?> getColumnClass(int column){
                if(column == 0) return ImageIcon.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row,int column){
                return false;
            }
        };

        tblInvoiceItem.setModel(model);

        tblInvoiceItem.setRowHeight(90);
        tblInvoiceItem.getColumnModel().getColumn(0).setMinWidth(100);
        tblInvoiceItem.getColumnModel().getColumn(0).setMaxWidth(130);
    }
    
    private Image scaleImage(Image img,int width,int height){

        java.awt.image.BufferedImage buffered =
            new java.awt.image.BufferedImage(
                width,height,
                java.awt.image.BufferedImage.TYPE_INT_ARGB
            );

        java.awt.Graphics2D g2 = buffered.createGraphics();

        g2.setRenderingHint(
            java.awt.RenderingHints.KEY_INTERPOLATION,
            java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        g2.setRenderingHint(
            java.awt.RenderingHints.KEY_RENDERING,
            java.awt.RenderingHints.VALUE_RENDER_QUALITY
        );

        g2.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.drawImage(img,0,0,width,height,null);

        g2.dispose();

        return buffered;
    }
    
    private void initImagePreview(){

        imagePreviewDialog = new JDialog();

        imagePreviewDialog.setUndecorated(true);

        imagePreviewLabel = new JLabel();

        imagePreviewLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePreviewLabel.setVerticalAlignment(JLabel.CENTER);

        imagePreviewDialog.add(imagePreviewLabel);

        imagePreviewDialog.setSize(600,600);
    }
    
    private void enableTableImageHover(){

        tblInvoiceItem.addMouseMotionListener(new java.awt.event.MouseAdapter(){

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e){

                int row = tblInvoiceItem.rowAtPoint(e.getPoint());
                int col = tblInvoiceItem.columnAtPoint(e.getPoint());

                if(row < 0 || col != 0){
                    imagePreviewDialog.setVisible(false);
                    return;
                }

                int modelRow = tblInvoiceItem.convertRowIndexToModel(row);

                if(modelRow >= currentItems.size()){
                    imagePreviewDialog.setVisible(false);
                    return;
                }

                InvoiceItem item = currentItems.get(modelRow);

                try{

                    ImageIcon icon =
                        new ImageIcon(new java.net.URL(item.getImage()));

                    Image scaled = scaleImage(icon.getImage(),600,600);

                    imagePreviewLabel.setIcon(new ImageIcon(scaled));

                    imagePreviewDialog.setLocationRelativeTo(null);

                    imagePreviewDialog.setVisible(true);

                }catch(Exception ex){
                    imagePreviewDialog.setVisible(false);
                }
            }
        });

        tblInvoiceItem.addMouseListener(new java.awt.event.MouseAdapter(){

            @Override
            public void mouseExited(java.awt.event.MouseEvent e){
                imagePreviewDialog.setVisible(false);
            }
        });
    }
    
    private void centerTable(JTable table, int... ignoreColumns){

        java.util.Set<Integer> ignore = new java.util.HashSet<>();
        for(int i : ignoreColumns){
            ignore.add(i);
        }

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        for(int i = 0; i < table.getColumnCount(); i++){

            if(ignore.contains(i)) continue;

            table.getColumnModel()
                 .getColumn(i)
                 .setCellRenderer(center);
        }

        DefaultTableCellRenderer header =
            (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();

        header.setHorizontalAlignment(SwingConstants.CENTER);
    } 
        
    private void exportInvoiceToPDF(int invoiceId) {
        try {
            // ===== LẤY INVOICE =====
            Invoice invoice = invoiceService.findById(invoiceId);

            if (invoice == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn!");
                return;
            }

            // ===== CHECK TRẠNG THÁI =====
//            if (invoice.getStatus() == null ||
//                !"Đã thanh toán".equalsIgnoreCase(invoice.getStatus().getLabel())) {
//                JOptionPane.showMessageDialog(this, "Chỉ xuất hóa đơn đã thanh toán!");
//                return;
//            }

            // ===== LẤY ROW TABLE =====
            int selectedRow = tblInvoice.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn!");
                return;
            }

            // ===== CUSTOMER (TỪ TABLE) =====
            String customerName = safe(String.valueOf(tblInvoice.getValueAt(selectedRow, 2)));
            String customerPhone = safe(String.valueOf(tblInvoice.getValueAt(selectedRow, 3)));
            String customerAddress = safe(String.valueOf(tblInvoice.getValueAt(selectedRow, 4)));

            if (customerName.isEmpty()) customerName = "Khách lẻ";

            // ===== DISCOUNT (CỘT 6) =====
            String discountText = String.valueOf(tblInvoice.getValueAt(selectedRow, 6));
            float discountAmount = 0;

            if (discountText != null && !discountText.isBlank()) {
                String number = discountText.replaceAll("[^0-9]", "");
                if (!number.isEmpty()) {
                    try {
                        discountAmount = Float.parseFloat(number);
                    } catch (Exception ignored) {}
                }
            }

            // ===== EMPLOYEE =====
            Employee emp = employeeService.findById(invoice.getEmployeeId());
            String employeeName = (emp != null) ? emp.getName() : "N/A";
            String employeeCode = (emp != null) ? emp.getCode() : "N/A";

            // ===== ITEMS =====
            List<InvoiceItem> items = cartService.findByInvoiceId(invoiceId, null);

            float total = 0;
            int totalQuantity = 0;

            for (InvoiceItem item : items) {
                total += item.getPrice() * item.getQuantity();
                totalQuantity += item.getQuantity();
            }

            float finalAmount = total - discountAmount;

            // ===== THÔNG TIN HÓA ĐƠN =====
            String invoiceCode = invoice.getCode();
            String paymentType = safe(invoice.getPaymentType());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String createdAt = invoice.getCreatedAt() != null
                    ? invoice.getCreatedAt().format(formatter)
                    : "N/A";

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

            Font titleFont = new Font(bf, 18, Font.BOLD);
            Font normalFont = new Font(bf, 12);
            Font boldFont = new Font(bf, 12, Font.BOLD);

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
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            float[] widths = {3f, 1.5f, 1.5f, 1.2f, 1.2f, 1f, 1.8f, 1.8f};
            table.setWidths(widths);

            addHeader(table, "Sản phẩm", boldFont);
            addHeader(table, "Danh mục", boldFont);
            addHeader(table, "Thương hiệu", boldFont);
            addHeader(table, "Màu sắc", boldFont);
            addHeader(table, "Kích thước", boldFont);
            addHeader(table, "SL", boldFont);
            addHeader(table, "Đơn giá", boldFont);
            addHeader(table, "Thành tiền", boldFont);

            for (InvoiceItem item : items) {
                addCell(table, safe(item.getProductName()), normalFont);
                addCellCenter(table, safe(item.getCategoryName()), normalFont);
                addCellCenter(table, safe(item.getBrandName()), normalFont);
                addCellCenter(table, safe(item.getColorName()), normalFont);
                addCellCenter(table, safe(item.getSizeName()), normalFont);
                addCellCenter(table, String.valueOf(item.getQuantity()), normalFont);
                addCellRight(table, moneyFormat.format(item.getPrice()), normalFont);
                addCellRight(table, moneyFormat.format(item.getPrice() * item.getQuantity()), normalFont);
            }

            // ===== TỔNG SỐ LƯỢNG =====
            PdfPCell totalLabel = new PdfPCell(new Phrase("Tổng số lượng", boldFont));
            totalLabel.setColspan(7);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(String.valueOf(totalQuantity), boldFont));
            totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(totalValue);

            document.add(table);

            // ===== TOTAL =====
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalTable.setSpacingBefore(10f);

            addCell(totalTable, "Tổng:", boldFont);
            addCellRight(totalTable, moneyFormat.format(total), normalFont);

            addCell(totalTable, "Giảm giá:", boldFont);
            addCellRight(totalTable, "- " + moneyFormat.format(discountAmount), normalFont);

            addCell(totalTable, "Thanh toán:", boldFont);
            addCellRight(totalTable, moneyFormat.format(finalAmount), boldFont);

            document.add(totalTable);

            document.close();

            JOptionPane.showMessageDialog(this, "Xuất PDF thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xuất PDF!");
        }
    }
    
    private void addHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addCellCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
    
    private void addCellRight(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
    
    private String safe(String value) {
        return value != null ? value : "";
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblInvoiceItem = new javax.swing.JTable();
        dcFrom = new com.toedter.calendar.JDateChooser();
        dcTo = new com.toedter.calendar.JDateChooser();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblInvoice = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        txtSearchInvoice = new javax.swing.JTextField();
        cbbColor = new javax.swing.JComboBox<>();
        cbbSize = new javax.swing.JComboBox<>();
        cbbCategory = new javax.swing.JComboBox<>();
        cbbBrand = new javax.swing.JComboBox<>();
        txtSearchInvoiceItem = new javax.swing.JTextField();
        cbbStatus = new javax.swing.JComboBox<>();
        btnInvoicePrint = new javax.swing.JButton();
        btnExportExcel = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Hoá đơn");

        tblInvoiceItem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ảnh", "Mã sản phẩm", "Tên sản phẩm", "Thương hiệu", "Danh mục", "Màu sắc", "Kích thước", "Số lượng", "Đơn giá"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblInvoiceItem);

        jLabel2.setText("Đến");

        jLabel3.setText("Từ");

        tblInvoice.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã hoá đơn", "Nhân viên bán", "Tên khách hàng", "Số điện thoại khách hàng", "Địa chỉ khách hàng", "Tổng tiền", "Giảm giá", "Thanh toán", "Ngày tạo", "Loại thanh toán", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, false, false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblInvoice.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblInvoiceMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblInvoice);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Hoá đơn chi tiết");

        txtSearchInvoice.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchInvoiceKeyReleased(evt);
            }
        });

        txtSearchInvoiceItem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSearchInvoiceItemKeyReleased(evt);
            }
        });

        btnInvoicePrint.setText("In hóa đơn");
        btnInvoicePrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInvoicePrintActionPerformed(evt);
            }
        });

        btnExportExcel.setText("Xuất excel");
        btnExportExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportExcelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1046, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbbBrand, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbbSize, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cbbColor, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtSearchInvoiceItem, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnInvoicePrint)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportExcel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtSearchInvoice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)))
                    .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(cbbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnInvoicePrint)
                        .addComponent(btnExportExcel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cbbColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbbBrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSearchInvoiceItem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblInvoiceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblInvoiceMouseClicked
        int row = tblInvoice.getSelectedRow();
        if(row == -1) return;

        int modelRow = tblInvoice.convertRowIndexToModel(row);
        Invoice invoice = invoices.get(modelRow);

        loadInvoiceItem(invoice.getId());
    }//GEN-LAST:event_tblInvoiceMouseClicked

    private void txtSearchInvoiceKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchInvoiceKeyReleased
        loadInvoiceTable();
    }//GEN-LAST:event_txtSearchInvoiceKeyReleased

    private void txtSearchInvoiceItemKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSearchInvoiceItemKeyReleased
        int row = tblInvoice.getSelectedRow();

        if (row < 0) return;

        int modelRow = tblInvoice.convertRowIndexToModel(row);

        Invoice invoice = invoices.get(modelRow);

        loadInvoiceItem(invoice.getId());
    }//GEN-LAST:event_txtSearchInvoiceItemKeyReleased

    private void btnInvoicePrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInvoicePrintActionPerformed
        int row = tblInvoice.getSelectedRow();

        if (row < 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn!");
            return;
        }

        int modelRow = tblInvoice.convertRowIndexToModel(row);
        Invoice invoice = invoices.get(modelRow);

        exportInvoiceToPDF(invoice.getId());      
    }//GEN-LAST:event_btnInvoicePrintActionPerformed

    private void btnExportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportExcelActionPerformed
        int row = tblInvoice.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn!");
            return;
        }

        try {
            int modelRow = tblInvoice.convertRowIndexToModel(row);
            Invoice invoice = invoices.get(modelRow);

            // ===== CHECK STATUS =====
            if (invoice.getStatus() == null ||
                !"Đã thanh toán".equalsIgnoreCase(invoice.getStatus().getLabel())) {
                JOptionPane.showMessageDialog(this, "Chỉ xuất hóa đơn đã thanh toán!");
                return;
            }

            List<InvoiceItem> items = cartService.findByInvoiceId(invoice.getId(), null);

            // ===== FILE =====
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Lưu file Excel");
            fc.setSelectedFile(new File("HoaDon_" + invoice.getCode() + ".xls"));

            if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

            File file = fc.getSelectedFile();
            if (!file.getName().endsWith(".xls")) {
                file = new File(file.getAbsolutePath() + ".xls");
            }

            // ===== CALCULATE =====
            double total = 0;
            int totalQty = 0;

            for (InvoiceItem item : items) {
                total += item.getPrice() * item.getQuantity();
                totalQty += item.getQuantity();
            }

            double discount = invoice.getDiscountAmount();
            double finalAmount = invoice.getTotalAmount();

            // ===== BUILD HTML =====
            StringBuilder sb = new StringBuilder();

            sb.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ")
              .append("xmlns:x='urn:schemas-microsoft-com:office:excel' ")
              .append("xmlns='http://www.w3.org/TR/REC-html40'>")
              .append("<head><meta charset='UTF-8'>")

              // ===== STYLE =====
              .append("<style>")
              .append("table {border-collapse: collapse; font-family: Arial;}")
              .append("th {background:#4682B4;color:white;font-weight:bold;font-size:12pt;}")
              .append("td {border:1px solid #999;padding:5px;}")
              .append(".right {text-align:right; mso-number-format:\"#,##0\";}")
              .append(".bold {font-weight:bold;}")
              .append("</style>")

              .append("</head><body>");

            // ===== HEADER =====
            sb.append("<h2 style='font-family:Arial'>HÓA ĐƠN THANH TOÁN</h2>");

            sb.append("<p>Mã: ").append(invoice.getCode()).append("</p>");
            sb.append("<p>Nhân viên: ").append(invoice.getEmployeeName()).append("</p>");
            sb.append("<p>Khách hàng: ").append(invoice.getCustomerName()).append("</p>");
            sb.append("<p>SĐT: ").append(invoice.getCustomerPhone()).append("</p>");
            sb.append("<p>Địa chỉ: ").append(invoice.getCustomerAddress()).append("</p>");
            sb.append("<p>Ngày: ")
              .append(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(dateFormat) : "")
              .append("</p>");

            sb.append("<p>Thanh toán: ")
              .append(safe(invoice.getPaymentType()))
              .append("</p>");

            // ===== TABLE =====
            sb.append("<br><table>");

            sb.append("<tr>")
              .append("<th>Sản phẩm</th>")
              .append("<th>Danh mục</th>")
              .append("<th>Thương hiệu</th>")
              .append("<th>Màu</th>")
              .append("<th>Size</th>")
              .append("<th>SL</th>")
              .append("<th>Đơn giá</th>")
              .append("<th>Thành tiền</th>")
              .append("</tr>");

            // ===== DATA =====
            for (InvoiceItem item : items) {
                double itemTotal = item.getPrice() * item.getQuantity();

                sb.append("<tr>")
                  .append("<td>").append(safe(item.getProductName())).append("</td>")
                  .append("<td>").append(safe(item.getCategoryName())).append("</td>")
                  .append("<td>").append(safe(item.getBrandName())).append("</td>")
                  .append("<td>").append(safe(item.getColorName())).append("</td>")
                  .append("<td>").append(safe(item.getSizeName())).append("</td>")
                  .append("<td class='right'>").append(item.getQuantity()).append("</td>")
                  .append("<td class='right'>").append((long)item.getPrice()).append("</td>")
                  .append("<td class='right'>").append((long)itemTotal).append("</td>")
                  .append("</tr>");
            }

            // ===== TOTAL ROW =====
            sb.append("<tr>")
              .append("<td colspan='5' class='bold'>Tổng cộng</td>")
              .append("<td class='bold right'>").append(totalQty).append("</td>")
              .append("<td></td>")
              .append("<td class='bold right'>").append((long)total).append("</td>")
              .append("</tr>");

            sb.append("</table>");

            // ===== SUMMARY =====
            sb.append("<br><table>");

            sb.append("<tr>")
              .append("<td class='bold'>Tổng tiền</td>")
              .append("<td class='right'>").append((long)total).append("</td>")
              .append("</tr>");

            sb.append("<tr>")
              .append("<td class='bold'>Giảm giá</td>")
              .append("<td class='right'>").append((long)discount).append("</td>")
              .append("</tr>");

            sb.append("<tr>")
              .append("<td class='bold'>Thanh toán</td>")
              .append("<td class='right'>").append((long)finalAmount).append("</td>")
              .append("</tr>");

            sb.append("</table>");

            sb.append("</body></html>");

            // ===== WRITE FILE =====
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {

                writer.write(sb.toString());

                JOptionPane.showMessageDialog(this,
                    "Xuất Excel thành công!\n" + file.getAbsolutePath());
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi xuất Excel!");
        }
    }//GEN-LAST:event_btnExportExcelActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportExcel;
    private javax.swing.JButton btnInvoicePrint;
    private javax.swing.JComboBox<String> cbbBrand;
    private javax.swing.JComboBox<String> cbbCategory;
    private javax.swing.JComboBox<String> cbbColor;
    private javax.swing.JComboBox<String> cbbSize;
    private javax.swing.JComboBox<String> cbbStatus;
    private com.toedter.calendar.JDateChooser dcFrom;
    private com.toedter.calendar.JDateChooser dcTo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblInvoice;
    private javax.swing.JTable tblInvoiceItem;
    private javax.swing.JTextField txtSearchInvoice;
    private javax.swing.JTextField txtSearchInvoiceItem;
    // End of variables declaration//GEN-END:variables
}
