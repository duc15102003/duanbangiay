package entity.filter;

import enums.OrderStatusEnum;
import java.time.LocalDateTime;

public class InvoiceFilter {
    
    private String search;

    private LocalDateTime fromCreatedDate;
    
    private LocalDateTime toCreatedDate;
    
    private OrderStatusEnum status;
    
    private String paymentType;

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public LocalDateTime getFromCreatedDate() {
        return fromCreatedDate;
    }

    public void setFromCreatedDate(LocalDateTime fromCreatedDate) {
        this.fromCreatedDate = fromCreatedDate;
    }

    public LocalDateTime getToCreatedDate() {
        return toCreatedDate;
    }

    public void setToCreatedDate(LocalDateTime toCreatedDate) {
        this.toCreatedDate = toCreatedDate;
    }

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }
}
