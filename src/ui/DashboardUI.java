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

        dcProductFrom.setDateFormatString("dd/MM/yyyy");
        dcProductTo.setDateFormatString("dd/MM/yyyy");
        dcProductTo.setEnabled(false); // ban đầu disable dcProductTo

        dcProductFrom.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                Date selected = dcProductFrom.getDate();
                dcProductTo.setEnabled(selected != null);
                if (selected == null) {
                    dcProductTo.setDate(null);
                }
                toggleBestSellerView();
            }
        });

        dcProductTo.getDateEditor().addPropertyChangeListener(evt -> {
            if ("date".equals(evt.getPropertyName())) {
                toggleBestSellerView();
            }
        });

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

        jComboBox1.addActionListener(e -> toggleBestSellerView());

        disablePopupIfNotDay(getSelectedType());
        reloadChart();
        loadBestSellerTable();

        toggleBestSellerView();
        btnExportThongKe.addActionListener(e -> exportToExcel());
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

        chartPanel.setPreferredSize(new java.awt.Dimension(1038, 635));

        pnChart.removeAll();
        pnChart.setLayout(new BorderLayout());

        pnChart.add(chartPanel, BorderLayout.NORTH);

        pnChart.revalidate();
        pnChart.repaint();
    }

    private void loadPieChart(String type, LocalDateTime from, LocalDateTime to) {
        JFreeChart chart = createPieChart(type, from, to);
        ChartPanel chartPanel = new ChartPanel(chart);

        chartPanel.setPreferredSize(new java.awt.Dimension(1038, 635));

        pnChart.removeAll();
        pnChart.setLayout(new BorderLayout());

        pnChart.add(chartPanel, BorderLayout.NORTH);

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
        List<RevenueDTO> data = dashboardService.getRevenueByPaymentType(from, to);

        DefaultPieDataset dataset = new DefaultPieDataset();
        java.util.Map<String, RevenueDTO> dataMap = new java.util.LinkedHashMap<>();

        if (data == null || data.isEmpty()) {
            dataset.setValue("Không có dữ liệu", 1);
        } else {
            for (RevenueDTO r : data) {
                String label = (r.getLabel() == null || r.getLabel().isBlank()) ? "Không xác định" : r.getLabel();
                String displayLabel = String.format("%s\n(%d Hoá đơn - %,.0f VNĐ)",
                    label,
                    r.getCount(),
                    r.getTotal()
                ).replace(",", ".");
                dataset.setValue(displayLabel, r.getTotal());
                dataMap.put(displayLabel, r);
            }
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Tỷ trọng doanh thu theo hình thức thanh toán",
            dataset, true, true, false
        );

        org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{0}\n{2}", 
            new java.text.DecimalFormat("#,##0"),
            new java.text.DecimalFormat("0.0%")
        ));

        plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 13));
        plot.setLabelBackgroundPaint(new java.awt.Color(255, 255, 255, 180));

        return chart;
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
    
    private LocalDateTime[] getBestSellerDateRange() {
        Date fromDate = dcProductFrom.getDate();
        Date toDate = dcProductTo.getDate();

        LocalDateTime from = fromDate != null
            ? fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay()
            : null;
        LocalDateTime to = toDate != null
            ? toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(23, 59, 59)
            : null;

        return new LocalDateTime[]{from, to};
    }
    
    private void loadBestSellerTable() {
        LocalDateTime[] range = getBestSellerDateRange();
        List<ProductVariant> list = productVariantService.getTop3BestSeller(range[0], range[1]);

        DefaultTableModel model = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Ảnh", "Mã sản phẩm", "Tên sản phẩm", "Thương hiệu",
                "Danh mục", "Kích thước", "Màu sắc",
                "Số lượng bán", "Tổng doanh thu"
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
        
        showBestSellerTable();
    }
    
    private void toggleBestSellerView() {
        Date fromDate = dcProductFrom.getDate();
        Date toDate = dcProductTo.getDate();

        if (fromDate != null && toDate != null && fromDate.after(toDate)) {
            JOptionPane.showMessageDialog(this, "'Đến' phải lớn hơn hoặc bằng 'Từ'!", "Lỗi chọn ngày", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String type = jComboBox1.getSelectedItem().toString();
        if ("Bảng".equals(type)) {
            loadBestSellerTable();
        } else {
            showBestSellerChart();
        }
    }
    
    private void showBestSellerTable() {
        pnBestSellerProduct.removeAll();
        pnBestSellerProduct.setLayout(new BorderLayout());
        pnBestSellerProduct.setPreferredSize(null);
        pnBestSellerProduct.setMinimumSize(null);
        pnBestSellerProduct.setMaximumSize(null);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(1038, 723));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(1038, 723));
        pnBestSellerProduct.add(jScrollPane1, BorderLayout.CENTER);
        pnBestSellerProduct.revalidate();
        pnBestSellerProduct.repaint();
    }
    
    private void showBestSellerChart() {
        LocalDateTime[] range = getBestSellerDateRange();
        List<ProductVariant> list = productVariantService.getTop3BestSeller(range[0], range[1]);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        if (list == null || list.isEmpty()) {
            dataset.addValue(0, "Doanh thu", "Không có dữ liệu");
        } else {
            for (ProductVariant pv : list) {
                String name = pv.getProductName();
                dataset.addValue(pv.getTotalRevenue(), "Doanh thu", name);
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Top sản phẩm bán chạy",
            "Sản phẩm",
            "Doanh thu (VNĐ)",
            dataset
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        
        chartPanel.setPreferredSize(new java.awt.Dimension(1038, 635));

        pnBestSellerProduct.removeAll();
        pnBestSellerProduct.setLayout(new BorderLayout());

        pnBestSellerProduct.add(chartPanel, BorderLayout.NORTH);

        pnBestSellerProduct.revalidate();
        pnBestSellerProduct.repaint();
    }
    
    private void exportToExcel() {
        String type = getSelectedType();
        Date fromDate = dcFrom.getDate();
        Date toDate = dcTo.getDate();

        if (fromDate != null) {
            LocalDate d = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            switch (type) {
                case "MONTH" -> fromDate = Date.from(d.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                case "YEAR"  -> fromDate = Date.from(d.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        }
        if (toDate != null) {
            LocalDate d = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            switch (type) {
                case "MONTH" -> toDate = Date.from(d.withDayOfMonth(d.lengthOfMonth()).atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());
                case "YEAR"  -> toDate = Date.from(d.withDayOfYear(d.lengthOfYear()).atTime(23,59,59).atZone(ZoneId.systemDefault()).toInstant());
            }
        }

        LocalDateTime from = toLocalDateTime(fromDate, false);
        LocalDateTime to   = toLocalDateTime(toDate, true);

        List<RevenueDTO> revenueList;
        switch (type) {
            case "DAY"   -> revenueList = dashboardService.getRevenueByDay(from, to);
            case "MONTH" -> revenueList = dashboardService.getRevenueByMonth(from, to);
            case "YEAR"  -> revenueList = dashboardService.getRevenueByYear(from, to);
            default      -> revenueList = List.of();
        }

        List<ProductVariant> productList = productVariantService.getAllSoldProducts(from, to);

        long totalQty = 0;
        double totalRev = 0;
        if (productList != null) {
            for (ProductVariant pv : productList) {
                totalQty += pv.getTotalQuantity();
                totalRev += pv.getTotalRevenue();
            }
        }

        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        fc.setDialogTitle("Lưu file Excel");
        fc.setSelectedFile(new java.io.File("ThongKe_"
            + java.time.LocalDate.now().toString().replace("-", "") + ".xls"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel (*.xls)", "xls"));

        if (fc.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;

        java.io.File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".xls")) file = new java.io.File(file.getAbsolutePath() + ".xls");

        String timeLabel = switch (type) {
            case "DAY"   -> "Ngày";
            case "MONTH" -> "Tháng";
            default      -> "Năm";
        };

        StringBuilder sb = new StringBuilder();
        sb.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' ")
          .append("xmlns:x='urn:schemas-microsoft-com:office:excel' ")
          .append("xmlns='http://www.w3.org/TR/REC-html40'>")
          .append("<head><meta charset='UTF-8'>")
          .append("<xml><x:ExcelWorkbook><x:ExcelWorksheets>")
          .append("<x:ExcelWorksheet><x:Name>Thong ke doanh thu</x:Name>")
          .append("<x:WorksheetOptions><x:Selected/></x:WorksheetOptions>")
          .append("</x:ExcelWorksheet>")
//          .append("<x:ExcelWorksheet><x:Name>San pham ban duoc</x:Name>")
//          .append("</x:ExcelWorksheet>")
          .append("</x:ExcelWorksheets></x:ExcelWorkbook></xml>")
          .append("</head><body>");

        sb.append("<table ss:Sheet='Thong ke doanh thu'>")
          .append("<tr>")
          .append("<th style='background:#4682B4;color:white;font-weight:bold;font-family:Arial;font-size:12pt'>").append(timeLabel).append("</th>")
          .append("<th style='background:#4682B4;color:white;font-weight:bold;font-family:Arial;font-size:12pt'>Số lượng sản phẩm bán</th>")
          .append("<th style='background:#4682B4;color:white;font-weight:bold;font-family:Arial;font-size:12pt'>Tổng doanh thu (VNĐ)</th>")
          .append("</tr>");

        if (revenueList != null) {
            for (RevenueDTO rev : revenueList) {
                sb.append("<tr>")
                  .append("<td style='font-family:Arial'>").append(rev.getLabel() != null ? rev.getLabel() : "").append("</td>")
                  .append("<td style='font-family:Arial;mso-number-format:\"#,##0\"'>").append(rev.getCount()).append("</td>")
                  .append("<td style='font-family:Arial;mso-number-format:\"#,##0\"'>").append((long) rev.getTotal()).append("</td>")
                  .append("</tr>");
            }
        }

        sb.append("<tr>")
          .append("<td style='font-weight:bold;font-family:Arial'>Tổng cộng</td>")
          .append("<td style='font-weight:bold;font-family:Arial;mso-number-format:\"#,##0\"'>").append(totalQty).append("</td>")
          .append("<td style='font-weight:bold;font-family:Arial;mso-number-format:\"#,##0\"'>").append((long) totalRev).append("</td>")
          .append("</tr>")
          .append("</table>");

        sb.append("<br><table ss:Sheet='San pham ban duoc'>")
          .append("<tr>");
        for (String h : new String[]{"Mã sản phẩm","Tên sản phẩm","Thương hiệu","Danh mục","Kích thước","Màu sắc","Số lượng bán","Tổng doanh thu (VNĐ)"}) {
            sb.append("<th style='background:#4682B4;color:white;font-weight:bold;font-family:Arial;font-size:12pt'>").append(h).append("</th>");
        }
        sb.append("</tr>");

        if (productList != null) {
            for (ProductVariant pv : productList) {
                sb.append("<tr>")
                  .append("<td style='font-family:Arial'>").append(pv.getProductCode()  != null ? pv.getProductCode()  : "").append("</td>")
                  .append("<td style='font-family:Arial'>").append(pv.getProductName()  != null ? pv.getProductName()  : "").append("</td>")
                  .append("<td style='font-family:Arial'>").append(pv.getBrandName()    != null ? pv.getBrandName()    : "").append("</td>")
                  .append("<td style='font-family:Arial'>").append(pv.getCategoryName() != null ? pv.getCategoryName() : "").append("</td>")
                  .append("<td style='font-family:Arial'>").append(pv.getSizeName()     != null ? pv.getSizeName()     : "").append("</td>")
                  .append("<td style='font-family:Arial'>").append(pv.getColorName()    != null ? pv.getColorName()    : "").append("</td>")
                  .append("<td style='font-family:Arial;mso-number-format:\"#,##0\"'>").append(pv.getTotalQuantity()).append("</td>")
                  .append("<td style='font-family:Arial;mso-number-format:\"#,##0\"'>").append((long) pv.getTotalRevenue()).append("</td>")
                  .append("</tr>");
            }
        }

        // Dòng tổng cộng dưới table
        sb.append("<tr>")
          .append("<td style='font-weight:bold;font-family:Arial' colspan='6'>Tổng cộng</td>")
          .append("<td style='font-weight:bold;font-family:Arial;mso-number-format:\"#,##0\"'>").append(totalQty).append("</td>")
          .append("<td style='font-weight:bold;font-family:Arial;mso-number-format:\"#,##0\"'>").append((long) totalRev).append("</td>")
          .append("</tr>")
          .append("</table>")
          .append("</body></html>");

        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
            JOptionPane.showMessageDialog(this,
                "Xuất file thành công!\n" + file.getAbsolutePath(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Lỗi khi xuất file: " + ex.getMessage(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        btnExportThongKe = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        pnBestSellerProduct = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblBestSellerProduct = new javax.swing.JTable();
        dcProductTo = new com.toedter.calendar.JDateChooser();
        dcProductFrom = new com.toedter.calendar.JDateChooser();

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

        btnExportThongKe.setText("Export");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnExportThongKe)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 263, Short.MAX_VALUE)
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
                        .addComponent(cbbTypeChart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnExportThongKe))
                    .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Thống kê doanh thu", jPanel1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bảng", "Biểu đồ" }));

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

        javax.swing.GroupLayout pnBestSellerProductLayout = new javax.swing.GroupLayout(pnBestSellerProduct);
        pnBestSellerProduct.setLayout(pnBestSellerProductLayout);
        pnBestSellerProductLayout.setHorizontalGroup(
            pnBestSellerProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1038, Short.MAX_VALUE)
        );
        pnBestSellerProductLayout.setVerticalGroup(
            pnBestSellerProductLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnBestSellerProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(dcProductFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dcProductTo, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcProductTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcProductFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnBestSellerProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
    private javax.swing.JButton btnExportThongKe;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cbbType;
    private javax.swing.JComboBox<String> cbbTypeChart;
    private com.toedter.calendar.JDateChooser dcFrom;
    private com.toedter.calendar.JDateChooser dcProductFrom;
    private com.toedter.calendar.JDateChooser dcProductTo;
    private com.toedter.calendar.JDateChooser dcTo;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel pnBestSellerProduct;
    private javax.swing.JPanel pnChart;
    private javax.swing.JTable tblBestSellerProduct;
    // End of variables declaration//GEN-END:variables
}
