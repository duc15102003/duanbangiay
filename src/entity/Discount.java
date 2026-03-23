package entity;

import enums.DiscountStatusEnum;
import java.time.LocalDateTime;

public class Discount extends BaseEntity {
    
    private String code;
    private String discountType; // Loại giảm giá (tiền mặt / %)
    private int discountValue; 
    private Integer maximumDiscount;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private DiscountStatusEnum status;
    private String discountCondition; // Điều kiện giảm
    private Integer quantity;

    public Discount() {
    }

    public Discount(String code, String discountType, int discountValue, Integer maximumDiscount, 
                    LocalDateTime startedAt, LocalDateTime endedAt, DiscountStatusEnum status,
                    String discountCondition, Integer quantity) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maximumDiscount = maximumDiscount;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
        this.discountCondition = discountCondition;
        this.quantity = quantity;
    }

    public Discount(String code, String discountType, int discountValue, Integer maximumDiscount, 
                    LocalDateTime startedAt, LocalDateTime endedAt, DiscountStatusEnum status, 
                    Integer id, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt,
                    String discountCondition, Integer quantity) {
        super(id, createdAt, updatedAt, deletedAt);
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maximumDiscount = maximumDiscount;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
        this.discountCondition = discountCondition;
        this.quantity = quantity;
    }

    public String getDiscountCondition() {
        return discountCondition;
    }

    public void setDiscountCondition(String discountCondition) {
        this.discountCondition = discountCondition;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public int getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(int discountValue) {
        this.discountValue = discountValue;
    }

    public Integer getMaximumDiscount() {
        return maximumDiscount;
    }

    public void setMaximumDiscount(Integer maximumDiscount) {
        this.maximumDiscount = maximumDiscount;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public DiscountStatusEnum getStatus() {
        return status;
    }

    public void setStatus(DiscountStatusEnum status) {
        this.status = status;
    }
}