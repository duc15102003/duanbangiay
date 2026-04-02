package ui;

import entity.ProductVariant;
import entity.RevenueDTO;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import service.DashboardService;
import service.ProductVariantService;

public class DashboardUI extends javax.swing.JPanel {

    private DashboardService dashboardService = new DashboardService();
    private ProductVariantService productVariantService = new ProductVariantService();

    public DashboardUI() {
        initComponents();

        cbbType.setSelectedIndex(1);
        updateDateFormat("MONTH");
        bindDateChooserEvents();

        btnSearch.addActionListener(e -> reloadChart());
        cbbType.addActionListener(e -> {
            String type = getSelectedType();
            dcFrom.setDate(null);
            dcTo.setDate(null);
            dcTo.setEnabled(false);
            updateDateFormat(type);
            reloadChart();
        });
        cbbTypeChart.addActionListener(e -> reloadChart());

        disablePopupIfNotDay(getSelectedType());
        reloadChart();
        loadBestSellerTable();
    }

    private void reloadChart() {
        String type = getSelectedType();
        Date fromDate = dcFrom.getDate();
        Date toDate = dcTo.getDate();

        if (fromDate != null) {
            LocalDate d = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            switch (type) {
                case "MONTH" -> fromDate = Date.from(d.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                case "YEAR" -> fromDate = Date.from(d.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        }

        if (toDate != null) {
            LocalDate d = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            switch (type) {
                case "MONTH" -> toDate = Date.from(d.withDayOfMonth(d.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                case "YEAR" -> toDate = Date.from(d.withDayOfYear(d.lengthOfYear()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            }
        }

        LocalDateTime from = toLocalDateTime(fromDate, false);
        LocalDateTime to = toLocalDateTime(toDate, true);

        if (from != null && to != null && from.isAfter(to)) {
            JOptionPane.showMessageDialog(this, "'Đến' phải lớn hơn hoặc bằng 'Từ'!", "Lỗi chọn ngày", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Biểu đồ hình tròn".equals(cbbTypeChart.getSelectedItem().toString())) {
            loadPieChart(type, from, to);
        } else {
            loadBarChart(type, from, to);
        }

        disablePopupIfNotDay(type);
    }

    private void loadBarChart(String type, LocalDateTime from, LocalDateTime to) {
        JFreeChart chart = createBarChart(type, from, to);
        ChartPanel chartPanel = new ChartPanel(chart);

        pnChart.removeAll();
        pnChart.setLayout(new BorderLayout());
        pnChart.add(chartPanel, BorderLayout.CENTER);
        pnChart.revalidate();
        pnChart.repaint();
    }

    private void loadPieChart(String type, LocalDateTime from, LocalDateTime to) {
        JFreeChart chart = createPieChart(type, from, to);
        ChartPanel chartPanel = new ChartPanel(chart);

        pnChart.removeAll();
        pnChart.setLayout(new BorderLayout());
        pnChart.add(chartPanel, BorderLayout.CENTER);
        pnChart.revalidate();
        pnChart.repaint();
    }

    private JFreeChart createBarChart(String type, LocalDateTime from, LocalDateTime to) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<RevenueDTO> data;

        switch (type) {
            case "DAY" -> data = dashboardService.getRevenueByDay(from, to);
            case "MONTH" -> data = dashboardService.getRevenueByMonth(from, to);
            case "YEAR" -> data = dashboardService.getRevenueByYear(from, to);
            default -> data = List.of();
        }

        if (data == null || data.isEmpty()) {
            dataset.addValue(0, "Doanh thu", "Không có dữ liệu");
        } else {
            for (RevenueDTO r : data) {
                String label = (r.getLabel() == null || r.getLabel().isBlank()) ? "N/A" : r.getLabel();
                dataset.addValue(r.getTotal(), "Doanh thu", label);
            }
        }

        return ChartFactory.createBarChart(
                "Doanh thu",
                switch (type) {
                    case "DAY" -> "Ngày";
                    case "MONTH" -> "Tháng";
                    default -> "Năm";
                },
                "VNĐ",
                dataset
        );
    }

    private JFreeChart createPieChart(String type, LocalDateTime from, LocalDateTime to) {
        List<RevenueDTO> data;
        switch (type) {
            case "DAY" -> data = dashboardService.getRevenueByDay(from, to);
            case "MONTH" -> data = dashboardService.getRevenueByMonth(from, to);
            case "YEAR" -> data = dashboardService.getRevenueByYear(from, to);
            default -> data = List.of();
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        if (data == null || data.isEmpty()) {
            dataset.setValue("Không có dữ liệu", 1);
        } else {
            for (RevenueDTO r : data) {
                String label = (r.getLabel() == null || r.getLabel().isBlank()) ? "N/A" : r.getLabel();
                dataset.setValue(label, r.getTotal());
            }
        }

        return ChartFactory.createPieChart("Tỷ trọng doanh thu", dataset, true, true, false);
    }

    private LocalDateTime toLocalDateTime(Date date, boolean endOfDay) {
        if (date == null) return null;
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return endOfDay ? localDate.atTime(23, 59, 59) : localDate.atStartOfDay();
    }

    private void bindDateChooserEvents() {
        dcFrom.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date selected = dcFrom.getDate();
                if (selected != null) {
                    dcFrom.getDateEditor().setDate(selected);
                    dcTo.setEnabled(true);
                } else {
                    dcTo.setEnabled(false);
                    dcTo.setDate(null);
                    dcTo.getDateEditor().setDate(null);
                }
            }
        });

        dcTo.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date selected = dcTo.getDate();
                if (selected != null) dcTo.getDateEditor().setDate(selected);
            }
        });
    }

    private void updateDateFormat(String type) {
        String pattern;
        boolean showDay;

        switch (type) {
            case "DAY" -> { pattern = "dd/MM/yyyy"; showDay = true; }
            case "MONTH" -> { pattern = "MM/yyyy"; showDay = false; }
            case "YEAR" -> { pattern = "yyyy"; showDay = false; }
            default -> { pattern = "dd/MM/yyyy"; showDay = true; }
        }

        dcFrom.setDateFormatString(pattern);
        dcTo.setDateFormatString(pattern);
        dcFrom.getJCalendar().getDayChooser().setVisible(showDay);
        dcTo.getJCalendar().getDayChooser().setVisible(showDay);
        dcFrom.getJCalendar().setWeekOfYearVisible(false);
        dcTo.getJCalendar().setWeekOfYearVisible(false);
        disablePopupIfNotDay(type);

        LocalDate now = LocalDate.now();
        Date from = dcFrom.getDate();
        Date to = dcTo.getDate();

        switch (type) {
            case "MONTH" -> {
                if (from == null) from = Date.from(now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (to == null) to = Date.from(now.withDayOfMonth(now.lengthOfMonth()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            }
            case "YEAR" -> {
                if (from == null) from = Date.from(now.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (to == null) to = Date.from(now.withDayOfYear(now.lengthOfYear()).atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            }
        }

        dcFrom.setDate(from);
        dcTo.setDate(to);
    }

    private String getSelectedType() {
        return switch (cbbType.getSelectedItem().toString()) {
            case "Ngày" -> "DAY";
            case "Tháng" -> "MONTH";
            case "Năm" -> "YEAR";
            default -> "MONTH";
        };
    }

    private void disablePopupIfNotDay(String type) {
        boolean enablePopup = "DAY".equals(type);
        dcFrom.getCalendarButton().setEnabled(enablePopup);
        dcTo.getCalendarButton().setEnabled(enablePopup);
    }
    
    private void loadBestSellerTable() {
        List<ProductVariant> list = productVariantService.getTop3BestSeller();

        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Ảnh", "Mã sản phẩm", "Tên sản phẩm", "Thương hiệu",
                "Danh mục", "Kích thước", "Màu sắc",
                "Số lượng bán", "Tổng doanh thu sản phẩm"
            }
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return javax.swing.Icon.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblBestSellerProduct.setModel(model);
        tblBestSellerProduct.setRowHeight(120);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) tblBestSellerProduct.getTableHeader().getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblBestSellerProduct.getTableHeader().setFont(
            tblBestSellerProduct.getTableHeader().getFont().deriveFont(Font.BOLD, 14f)
        );

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (ProductVariant pv : list) {
            model.addRow(new Object[]{
                null,
                pv.getProductCode(),
                pv.getProductName(),
                pv.getBrandName(),
                pv.getCategoryName(),
                pv.getSizeName(),
                pv.getColorName(),
                pv.getTotalQuantity(),
                String.format("%,.0f", pv.getTotalRevenue()).replace(",", ".")
            });
        }

        for (int i = 1; i < tblBestSellerProduct.getColumnCount(); i++) {
            tblBestSellerProduct.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        tblBestSellerProduct.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = tblBestSellerProduct.rowAtPoint(e.getPoint());
                int col = tblBestSellerProduct.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 2) {
                    Object value = tblBestSellerProduct.getValueAt(row, col);
                    tblBestSellerProduct.setToolTipText(value != null ? value.toString() : null);
                } else {
                    tblBestSellerProduct.setToolTipText(null);
                }
            }
        });

        for (int i = 0; i < list.size(); i++) {
            final int rowIndex = i;
            final String imageUrl = list.get(i).getImage();

            if (imageUrl == null || imageUrl.isBlank()) continue;

            new Thread(() -> {
                try {
                    ImageIcon icon = new ImageIcon(new java.net.URL(imageUrl));
                    Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    ImageIcon finalIcon = new ImageIcon(scaled);

                    SwingUtilities.invokeLater(() ->
                        tblBestSellerProduct.setValueAt(finalIcon, rowIndex, 0)
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        btnSearch = new javax.swing.JButton();
        dcTo = new com.toedter.calendar.JDateChooser();
        dcFrom = new com.toedter.calendar.JDateChooser();
        cbbType = new javax.swing.JComboBox<>();
        cbbTypeChart = new javax.swing.JComboBox<>();
        pnChart = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBestSellerProduct = new javax.swing.JTable();

        btnSearch.setText("Tìm kiếm");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        cbbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ngày", "Tháng", "Năm" }));

        cbbTypeChart.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Biểu đồ cột", "Biểu đồ hình tròn" }));

        javax.swing.GroupLayout pnChartLayout = new javax.swing.GroupLayout(pnChart);
        pnChart.setLayout(pnChartLayout);
        pnChartLayout.setHorizontalGroup(
            pnChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnChartLayout.setVerticalGroup(
            pnChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 653, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(341, Short.MAX_VALUE)
                .addComponent(cbbTypeChart, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cbbType, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearch)
                .addContainerGap())
            .addComponent(pnChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cbbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbbTypeChart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Thống kê doanh thu", jPanel1);

        tblBestSellerProduct.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        tblBestSellerProduct.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Ảnh", "Mã sản phẩm", "Tên sản phẩm", "Thương hiệu", "Danh mục", "Kích thước", "Màu sắc", "Số lượng được bán ra", "Tổng doanh thu sản phẩm"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblBestSellerProduct);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1026, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Sản phẩm bán chạy", jPanel2);

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

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cbbType;
    private javax.swing.JComboBox<String> cbbTypeChart;
    private com.toedter.calendar.JDateChooser dcFrom;
    private com.toedter.calendar.JDateChooser dcTo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pnChart;
    private javax.swing.JTable tblBestSellerProduct;
    // End of variables declaration//GEN-END:variables
}
