package entity.filter;

import enums.DiscountStatusEnum;
import java.time.LocalDateTime;

public class DiscountFilter {
    
    private String search;
    
    private LocalDateTime fromStartedAt;
    
    private LocalDateTime toEndedAt;
    
    private DiscountStatusEnum status;

    public DiscountFilter() {
    }

    public DiscountFilter(String search, LocalDateTime fromStartedAt, LocalDateTime toEndedAt, DiscountStatusEnum status) {
        this.search = search;
        this.fromStartedAt = fromStartedAt;
        this.toEndedAt = toEndedAt;
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public LocalDateTime getFromStartedAt() {
        return fromStartedAt;
    }

    public void setFromStartedAt(LocalDateTime fromStartedAt) {
        this.fromStartedAt = fromStartedAt;
    }

    public LocalDateTime getToEndedAt() {
        return toEndedAt;
    }

    public void setToEndedAt(LocalDateTime toEndedAt) {
        this.toEndedAt = toEndedAt;
    }

    public DiscountStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DiscountStatusEnum status) {
        this.status = status;
    }
}
