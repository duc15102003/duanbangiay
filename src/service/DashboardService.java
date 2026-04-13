package service;

import dao.DashboardDAO;
import entity.DashboardSummaryDTO;
import entity.RevenueDTO;
import java.time.LocalDateTime;
import java.util.List;

public class DashboardService {

    private final DashboardDAO dao = new DashboardDAO();

    public List<RevenueDTO> getRevenueByDay(LocalDateTime from, LocalDateTime to) {
        return dao.getRevenue("DAY", from, to);
    }

    public List<RevenueDTO> getRevenueByMonth(LocalDateTime from, LocalDateTime to) {
        return dao.getRevenue("MONTH", from, to);
    }

    public List<RevenueDTO> getRevenueByYear(LocalDateTime from, LocalDateTime to) {
        return dao.getRevenue("YEAR", from, to);
    }
    
    public List<RevenueDTO> getRevenueByPaymentType(LocalDateTime from, LocalDateTime to) {
        return dao.getRevenueByPaymentType(from, to);
    }
    
    public DashboardSummaryDTO getDashboardSummary(){
        return dao.getDashboardSummary();
    }
}
