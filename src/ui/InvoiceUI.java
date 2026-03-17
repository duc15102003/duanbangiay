package ui;

import dao.CartDAO;
import entity.Invoice;
import entity.InvoiceItem;
import entity.filter.InvoiceFilter;
import java.awt.Image;
import java.text.DecimalFormat;
import java.time.ZoneId;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.SwingConstants;
import java.time.format.DateTimeFormatter;

public class InvoiceUI extends javax.swing.JPanel {
    
    private final CartDAO cartDAO = new CartDAO();

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

        invoices = list.stream()
                .filter(i -> i.getStatus() != enums.OrderStatusEnum.PENDING_PAYMENT)
                .toList();

        DefaultTableModel model = (DefaultTableModel) tblInvoice.getModel();
        model.setRowCount(0);

        for (Invoice i : invoices) {

            String createdDate = "";

            if (i.getCreatedAt() != null) {
                createdDate = i.getCreatedAt().format(dateFormat);
            }

            model.addRow(new Object[]{
                i.getCode(),
                i.getEmployeeName(),
                i.getCustomerName(),
                i.getCustomerPhone(),
                i.getCustomerAddress(),
                moneyFormat.format(i.getTotalAmount()),
                createdDate,
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

            model.addRow(new Object[]{
                null,
                item.getProductName(),
                item.getBrandName(),
                item.getCategoryName(),
                item.getColorName(),
                item.getSizeName(),
                item.getQuantity(),
                moneyFormat.format(item.getPrice())
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
                "Ảnh","Tên sản phẩm","Thương hiệu","Danh mục","Màu sắc","Kích thước","Số lượng","Đơn giá"
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
        tblInvoiceItem.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblInvoiceItem.getColumnModel().getColumn(0).setWidth(100);
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

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Hoá đơn");

        tblInvoiceItem.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ảnh", "Tên sản phẩm", "Thương hiệu", "Danh mục", "Màu sắc", "Kích thước", "Số lượng", "Đơn giá"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
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
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Mã hoá đơn", "Nhân viên bán", "Tên khách hàng", "Số điện thoại khách hàng", "Địa chỉ khách hàng", "Tổng tiền", "Ngày tạo", "Trạng thái"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
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
                        .addGap(0, 0, Short.MAX_VALUE)
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
                    .addComponent(jLabel3))
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cbbBrand;
    private javax.swing.JComboBox<String> cbbCategory;
    private javax.swing.JComboBox<String> cbbColor;
    private javax.swing.JComboBox<String> cbbSize;
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
