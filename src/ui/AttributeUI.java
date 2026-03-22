package ui;

public class AttributeUI extends javax.swing.JPanel {

    public AttributeUI() {
        initComponents();
        
        loadBrandUI();
        loadCategoryUI();
        loadColorUI();
        loadSizeUI();
    }
    
    private void loadBrandUI() {
    jPanel1.removeAll();

    BrandUI ui = new BrandUI(null);

    jPanel1.setLayout(new java.awt.BorderLayout());
    jPanel1.add(ui, java.awt.BorderLayout.CENTER);

    jPanel1.revalidate();
    jPanel1.repaint();
}

private void loadCategoryUI() {
    pnCategory.removeAll();

    CategoryUI ui = new CategoryUI(null);

    pnCategory.setLayout(new java.awt.BorderLayout());
    pnCategory.add(ui, java.awt.BorderLayout.CENTER);

    pnCategory.revalidate();
    pnCategory.repaint();
}

private void loadColorUI() {
    pnColor.removeAll();

    ColorUI ui = new ColorUI(null);

    pnColor.setLayout(new java.awt.BorderLayout());
    pnColor.add(ui, java.awt.BorderLayout.CENTER);

    pnColor.revalidate();
    pnColor.repaint();
}

private void loadSizeUI() {
    pnSize.removeAll();

    SizeUI ui = new SizeUI(null);

    pnSize.setLayout(new java.awt.BorderLayout());
    pnSize.add(ui, java.awt.BorderLayout.CENTER);

    pnSize.revalidate();
    pnSize.repaint();
}

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnBrand = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        pnCategory = new javax.swing.JPanel();
        pnColor = new javax.swing.JPanel();
        pnSize = new javax.swing.JPanel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1038, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );

        pnBrand.addTab("Thương hiệu", jPanel1);

        javax.swing.GroupLayout pnCategoryLayout = new javax.swing.GroupLayout(pnCategory);
        pnCategory.setLayout(pnCategoryLayout);
        pnCategoryLayout.setHorizontalGroup(
            pnCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1038, Short.MAX_VALUE)
        );
        pnCategoryLayout.setVerticalGroup(
            pnCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );

        pnBrand.addTab("Danh mục", pnCategory);

        javax.swing.GroupLayout pnColorLayout = new javax.swing.GroupLayout(pnColor);
        pnColor.setLayout(pnColorLayout);
        pnColorLayout.setHorizontalGroup(
            pnColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1038, Short.MAX_VALUE)
        );
        pnColorLayout.setVerticalGroup(
            pnColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );

        pnBrand.addTab("Màu sắc", pnColor);

        javax.swing.GroupLayout pnSizeLayout = new javax.swing.GroupLayout(pnSize);
        pnSize.setLayout(pnSizeLayout);
        pnSizeLayout.setHorizontalGroup(
            pnSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1038, Short.MAX_VALUE)
        );
        pnSizeLayout.setVerticalGroup(
            pnSizeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 671, Short.MAX_VALUE)
        );

        pnBrand.addTab("Kích thước", pnSize);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnBrand)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnBrand)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane pnBrand;
    private javax.swing.JPanel pnCategory;
    private javax.swing.JPanel pnColor;
    private javax.swing.JPanel pnSize;
    // End of variables declaration//GEN-END:variables
}
