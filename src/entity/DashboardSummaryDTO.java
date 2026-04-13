package entity;

public class DashboardSummaryDTO {
    private int todayInvoiceCount;
    private double todayRevenue;
    private int yesterdayInvoiceCount;

    public DashboardSummaryDTO(int todayInvoiceCount, double todayRevenue, int yesterdayInvoiceCount) {
        this.todayInvoiceCount = todayInvoiceCount;
        this.todayRevenue = todayRevenue;
        this.yesterdayInvoiceCount = yesterdayInvoiceCount;
    }

    public int getTodayInvoiceCount() {
        return todayInvoiceCount;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public int getYesterdayInvoiceCount() {
        return yesterdayInvoiceCount;
    }
}
