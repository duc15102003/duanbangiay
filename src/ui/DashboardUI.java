package ui;

import entity.RevenueDTO;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import service.DashboardService;

public class DashboardUI extends javax.swing.JPanel {
    
    private DashboardService dashboardService = new DashboardService();

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

        disablePopupIfNotDay(getSelectedType());
        reloadChart();
    }

    private void reloadChart() {
        String type = getSelectedType();
        Date fromDate = dcFrom.getDate();
        Date toDate = dcTo.getDate();

        if (fromDate != null) {
            LocalDate d = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            switch (type) {
                case "MONTH":
                    fromDate = Date.from(d.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                    break;
                case "YEAR":
                    fromDate = Date.from(d.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                    break;
            }
        }

        if (toDate != null) {
            LocalDate d = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            switch (type) {
                case "MONTH":
                    toDate = Date.from(d.withDayOfMonth(d.lengthOfMonth())
                            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                    break;
                case "YEAR":
                    toDate = Date.from(d.withDayOfYear(d.lengthOfYear())
                            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                    break;
            }
        }

        LocalDateTime from = toLocalDateTime(fromDate, false);
        LocalDateTime to = toLocalDateTime(toDate, true);

        if (from != null && to != null && from.isAfter(to)) {
            JOptionPane.showMessageDialog(this, "'Đến' phải lớn hơn hoặc bằng 'Từ'!", "Lỗi chọn ngày", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadChart(type, from, to);
        disablePopupIfNotDay(type);
    }

    private void loadChart(String type, LocalDateTime from, LocalDateTime to) {
        JFreeChart chart = createBarChart(type, from, to);
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
            case "DAY":
                data = dashboardService.getRevenueByDay(from, to);
                break;
            case "MONTH":
                data = dashboardService.getRevenueByMonth(from, to);
                break;
            case "YEAR":
                data = dashboardService.getRevenueByYear(from, to);
                break;
            default:
                data = List.of();
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
            case "DAY":
                pattern = "dd/MM/yyyy";
                showDay = true;
                break;
            case "MONTH":
                pattern = "MM/yyyy";
                showDay = false;
                break;
            case "YEAR":
                pattern = "yyyy";
                showDay = false;
                break;
            default:
                pattern = "dd/MM/yyyy";
                showDay = true;
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
            case "MONTH":
                if (from == null)
                    from = Date.from(now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (to == null)
                    to = Date.from(now.withDayOfMonth(now.lengthOfMonth())
                            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case "YEAR":
                if (from == null)
                    from = Date.from(now.withDayOfYear(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (to == null)
                    to = Date.from(now.withDayOfYear(now.lengthOfYear())
                            .atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                break;
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
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnChart = new javax.swing.JPanel();
        cbbType = new javax.swing.JComboBox<>();
        dcFrom = new com.toedter.calendar.JDateChooser();
        dcTo = new com.toedter.calendar.JDateChooser();
        btnSearch = new javax.swing.JButton();

        javax.swing.GroupLayout pnChartLayout = new javax.swing.GroupLayout(pnChart);
        pnChart.setLayout(pnChartLayout);
        pnChartLayout.setHorizontalGroup(
            pnChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnChartLayout.setVerticalGroup(
            pnChartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 649, Short.MAX_VALUE)
        );

        cbbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Ngày", "Tháng", "Năm" }));

        btnSearch.setText("Tìm kiếm");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbbType, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(183, Short.MAX_VALUE))
            .addComponent(pnChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cbbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dcFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dcTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnSearch))
                .addGap(18, 18, 18)
                .addComponent(pnChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cbbType;
    private com.toedter.calendar.JDateChooser dcFrom;
    private com.toedter.calendar.JDateChooser dcTo;
    private javax.swing.JPanel pnChart;
    // End of variables declaration//GEN-END:variables
}
