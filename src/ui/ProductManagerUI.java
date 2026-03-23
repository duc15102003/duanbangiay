package ui;

import entity.Product;
import entity.ProductVariant;
import entity.filter.ProductVariantFilter;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import service.ProductService;
import service.ProductVariantService;

public class ProductManagerUI extends javax.swing.JPanel {
    
    private ProductService productService = new ProductService();
    private ProductVariantService productVariantService = new ProductVariantService();
    
    private ProductUI productUI;
    private ProductVariantUI productVariantUI;
    
    private List<Product> listProduct = new ArrayList<>();
    private Map<Integer, Product> productMap = new HashMap<>();
    
    private Map<String, ImageIcon> imageCache = new HashMap<>();

    private final java.util.concurrent.ExecutorService imageLoader = Executors.newFixedThreadPool(4);

    public ProductManagerUI() {
        initComponents();

        initProductTable();
        initProductVariantTable();

        loadProductUI();
        loadProductVariantUI();

        loadProductTable();

        jTabbedPane1.addChangeListener(e -> {
        int selectedIndex = jTabbedPane1.getSelectedIndex();

        switch(selectedIndex){
            case 0:
                loadProductTable();
                int row = tblProduct.getSelectedRow();
                if(row >= 0){
                    int productId = (int) tblProduct.getValueAt(row, 5);
                    loadVariantByProduct(productId);
                }
                break;
            case 1: // Tab ProductUI
                loadProductUI();
                if (productUI != null) {
                    productUI.reloadAllData();
                }
                break;
            case 2: // Tab ProductVariantUI
                loadProductVariantUI();
                if (productVariantUI != null) {
                    productVariantUI.reloadAllData(); 
                }
                break;
        }
    });
    }
    
    private void loadProductUI() {
        pnProduct.removeAll();
        if (productUI == null) {
            productUI = new ProductUI(this);
        }
        pnProduct.setLayout(new java.awt.BorderLayout());
        pnProduct.add(productUI, java.awt.BorderLayout.CENTER);
        pnProduct.revalidate();
        pnProduct.repaint();
    }
    
    private void loadProductVariantUI() {
        pnProductDetail.removeAll();
        if (productVariantUI == null) {
            productVariantUI = new ProductVariantUI();
        }
        pnProductDetail.setLayout(new java.awt.BorderLayout());
        pnProductDetail.add(productVariantUI, java.awt.BorderLayout.CENTER);
        pnProductDetail.revalidate();
        pnProductDetail.repaint();
    }
    
    public javax.swing.JTabbedPane getTabbedPane() {
        return jTabbedPane1;
    }

    public ProductVariantUI getProductVariantUI() {
        return productVariantUI;
    }
    
    private void initProductTable() {

        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Mã", "Tên", "Danh mục", "Thương hiệu", "Trạng thái", "Id"
            }
        ){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        tblProduct.setModel(model);

        tblProduct.getColumnModel().getColumn(5).setMinWidth(0);
        tblProduct.getColumnModel().getColumn(5).setMaxWidth(0);
        tblProduct.getColumnModel().getColumn(5).setWidth(0);
    }
    
    private void loadProductTable() {

        listProduct = productService.findAll(null);
        productMap.clear();

        DefaultTableModel model = (DefaultTableModel) tblProduct.getModel();
        model.setRowCount(0);

        for(Product p : listProduct){

            productMap.put(p.getId(), p);

            model.addRow(new Object[]{
                p.getCode(),
                p.getName(),
                p.getCategoryName(),
                p.getBrandName(),
                p.getStatus(),
                p.getId()
            });
        }
    }
    
    private void initProductVariantTable() {

        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Ảnh","Mã", "Tên", "Màu sắc", "Kích thước", "Số lượng", "Giá", "Id"
            }
        ){
            @Override
            public Class<?> getColumnClass(int column){
                if(column == 0) return javax.swing.Icon.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        };

        tblProductVariant.setModel(model);

        tblProductVariant.setRowHeight(90);

        tblProductVariant.getColumnModel().getColumn(7).setMinWidth(0);
        tblProductVariant.getColumnModel().getColumn(7).setMaxWidth(0);
        tblProductVariant.getColumnModel().getColumn(7).setWidth(0);
    }
    
    private void loadVariantByProduct(int productId) {

        ProductVariantFilter filter = new ProductVariantFilter();
        filter.setProductId(productId);

        List<ProductVariant> list = productVariantService.findAll(filter);

        DefaultTableModel model = (DefaultTableModel) tblProductVariant.getModel();
        model.setRowCount(0);
        
        for(ProductVariant pv : list){
            
            ImageIcon cached = imageCache.get(pv.getImage());

            model.addRow(new Object[]{
                cached,
                pv.getProductCode(),
                pv.getProductName(),
                pv.getColorName(),
                pv.getSizeName(),
                pv.getQuantity(),
                formatMoney(pv.getPrice()),
                pv.getId()
            });

            loadImageAsync(pv.getImage(), pv.getId());
        }
    }
    
    private String formatMoney(float price){
        return String.format("%,.0f", price).replace(",", ".");
    }

    private Image scaleImage(Image img, int width, int height){

        BufferedImage buffered =
                new java.awt.image.BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = buffered.createGraphics();

        g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2.drawImage(img, 0, 0, width, height, null);

        g2.dispose();

        return buffered;
    }
    
    private ImageIcon loadImageAsync(String url, int productVariantId){

        if(url == null || url.isBlank()) return null;

        if(imageCache.containsKey(url)){
            return imageCache.get(url);
        }

        imageLoader.submit(() -> {
            try{
                ImageIcon icon = new ImageIcon(new java.net.URL(url));
                Image scaled = scaleImage(icon.getImage(), 90, 90);
                ImageIcon finalIcon = new ImageIcon(scaled);

                imageCache.put(url, finalIcon);

                SwingUtilities.invokeLater(() -> {

                    for(int i = 0; i < tblProductVariant.getRowCount(); i++){

                        int id = (int) tblProductVariant.getValueAt(i, 7);

                        if(id == productVariantId){
                            tblProductVariant.setValueAt(finalIcon, i, 0);
                            break;
                        }
                    }

                });

            }catch(Exception e){
                e.printStackTrace();
            }
        });

        return null;
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        pnListProduct = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProductVariant = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblProduct = new javax.swing.JTable();
        pnProduct = new javax.swing.JPanel();
        pnProductDetail = new javax.swing.JPanel();

        tblProductVariant.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblProductVariant);

        tblProduct.setModel(new javax.swing.table.DefaultTableModel(
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
        tblProduct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProductMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblProduct);

        javax.swing.GroupLayout pnListProductLayout = new javax.swing.GroupLayout(pnListProduct);
        pnListProduct.setLayout(pnListProductLayout);
        pnListProductLayout.setHorizontalGroup(
            pnListProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnListProductLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1026, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(pnListProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnListProductLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1026, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pnListProductLayout.setVerticalGroup(
            pnListProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnListProductLayout.createSequentialGroup()
                .addContainerGap(316, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(pnListProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnListProductLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(361, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Danh sách sản phẩm", pnListProduct);

        javax.swing.GroupLayout pnProductLayout = new javax.swing.GroupLayout(pnProduct);
        pnProduct.setLayout(pnProductLayout);
        pnProductLayout.setHorizontalGroup(
            pnProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1038, Short.MAX_VALUE)
        );
        pnProductLayout.setVerticalGroup(
            pnProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 661, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Sản phẩm", pnProduct);

        javax.swing.GroupLayout pnProductDetailLayout = new javax.swing.GroupLayout(pnProductDetail);
        pnProductDetail.setLayout(pnProductDetailLayout);
        pnProductDetailLayout.setHorizontalGroup(
            pnProductDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1038, Short.MAX_VALUE)
        );
        pnProductDetailLayout.setVerticalGroup(
            pnProductDetailLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 661, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Sản phẩm biến thể", pnProductDetail);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblProductMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProductMouseClicked
        int row = tblProduct.getSelectedRow();
        if(row < 0) return;

        int productId = (int) tblProduct.getValueAt(row, 5);

        loadVariantByProduct(productId);
    }//GEN-LAST:event_tblProductMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pnListProduct;
    private javax.swing.JPanel pnProduct;
    private javax.swing.JPanel pnProductDetail;
    private javax.swing.JTable tblProduct;
    private javax.swing.JTable tblProductVariant;
    // End of variables declaration//GEN-END:variables
}
